class DisplayMqttTimetable(object):
    def __init__(self):
        import os, sys, glob, uuid, time, socket, subprocess, webbrowser
        from java.lang import System
        import jmri
        try:
            import json
        except ImportError:
            import simplejson as json
        import urllib2
        print("Check init list is:", type(list))

        self.os = os
        self.sys = sys
        self.glob = glob
        self.uuid = uuid
        self.time = time
        self.socket = socket
        self.subprocess = subprocess
        self.webbrowser = webbrowser
        self.System = System
        self.jmri = jmri
        self.json = json
        self.urllib2 = urllib2

        self.HOST = "127.0.0.1"
        self.PORT = 1880
        # self.START_DELAY = 8
        self.NODE_RED_CMD = None
        self._stdout_fnull = None
        self._stderr_fnull = None
        self._node_red_proc = None

        self.run()


    def is_windows_jython(self):
        os_name = self.System.getProperty("os.name") or ""
        return os_name.lower().startswith("windows")

    def find_node_red(self):
        is_windows = self.is_windows_jython()
        probe = ['where', 'node-red'] if is_windows else ['which', 'node-red']
        try:
            out = self.subprocess.check_output(probe, stderr=self.subprocess.STDOUT)
        except OSError:
            return None
        paths = out.decode('utf-8', 'ignore').strip().splitlines()
        if is_windows:
            for p in paths:
                if p.lower().endswith('.cmd') and self.os.path.isfile(p):
                    return p
        for p in paths:
            if self.os.path.isfile(p):
                return p
        return None

    def verify_node_red(self):
        path = self.find_node_red()
        if path:
            self.NODE_RED_CMD = path
        else:
            self.NODE_RED_CMD = 'node-red'
            self.sys.stderr.write("> Warning: node-red not found. Will attempt 'node-red' from PATH.\n")

    def is_running(self):
        try:
            s = self.socket.create_connection((self.HOST, self.PORT), 1)
            s.close()
            return True
        except:
            return False

    def start_node_red(self):
        if self._stdout_fnull is None:
            self._stdout_fnull = open(self.os.devnull, "w")
            self._stderr_fnull = open(self.os.devnull, "w")

        self._node_red_proc = self.subprocess.Popen(
            [self.NODE_RED_CMD],
            stdout=self._stdout_fnull,
            stderr=self._stderr_fnull,
            close_fds=True
        )

    def wait_for_node_red(self, timeout=15):
        print("> Waiting for Node-RED to start...")
        start_time = self.time.time()
        while self.time.time() - start_time < timeout:
            if self.is_running():
                print("> Node-RED is now responsive.")
                return True
            self.time.sleep(1)
        print("> Timed out waiting for Node-RED.")
        return False


    def build_new_flows(self):
        new_nodes = []
        SCRIPT_DIR = self.jmri.util.FileUtil.getExternalFilename('preference:dispatcher/mqtt_timetables/')
        for filepath in self.glob.glob(self.os.path.join(SCRIPT_DIR, "*.json")):
            name = self.os.path.splitext(self.os.path.basename(filepath))[0]
            label = name.replace(" ", "-")
            tab_id = self.uuid.uuid4().hex
            new_nodes.append({"id": tab_id, "type": "tab", "label": label, "disabled": False, "info": ""})

            import codecs
            with codecs.open(filepath, mode='r', encoding='utf-8') as f:
                try:
                    raw = self.json.load(f)
                except Exception as e:
                    raise IOError("Failed to parse JSON from %s: %s" % (filepath, e))
            arr = raw if isinstance(raw, list) else raw.get("flows", [])
            nodes_only = [n for n in arr if n.get("type") != "tab"]
            id_map = dict((n["id"], self.uuid.uuid4().hex) for n in nodes_only)

            for node in nodes_only:
                old_id = node["id"]
                had_z = ("z" in node)
                node["id"] = id_map[old_id]
                if had_z:
                    node["z"] = tab_id
                wires = node.get("wires")
                if isinstance(wires, list):
                    node["wires"] = [[id_map.get(dest, dest) for dest in link] for link in wires]
                for k, v in node.items():
                    if isinstance(v, basestring) and v in id_map:
                        node[k] = id_map[v]
                new_nodes.append(node)
        return new_nodes

    def http_get_flows(self):
        req = self.urllib2.Request("http://%s:%d/flows" % (self.HOST, self.PORT))
        res = self.urllib2.urlopen(req)
        return self.json.loads(res.read())

    def http_post_flows(self, flows):
        payload = self.json.dumps(flows)
        req = self.urllib2.Request("http://%s:%d/flows" % (self.HOST, self.PORT), data=payload,
                                   headers={"Content-Type": "application/json"})
        self.urllib2.urlopen(req).read()

    def open_all_tabs(self, tab_labels):
        for label in tab_labels:
            url = "http://%s:%d/%s" % (self.HOST, self.PORT, label)
            self.time.sleep(0.3)
            self.webbrowser.open_new_tab(url)

    def run(self):
        self.verify_node_red()
        preserve = True  # Replace with user prompt if needed
        new_nodes = self.build_new_flows()
        tab_labels = [n["label"] for n in new_nodes if n.get("type") == "tab"]
        if not self.is_running():
            self.start_node_red()
            print "node red started"
            # self.time.sleep(self.START_DELAY)
            if not self.wait_for_node_red():
                print("> WARNING: Node-RED did not start in time.")
                return
        else:
            print "node red already running", "preserve", preserve
        if not preserve:
            self.http_post_flows(new_nodes)
            self.open_all_tabs(tab_labels)
            return
        existing = self.http_get_flows()
        remove_ids = set(n["id"] for n in existing if n.get("type") == "tab" and n.get("label") in tab_labels)
        kept = [n for n in existing if not (n.get("type") == "tab" and n["id"] in remove_ids)
                and not ("z" in n and n["z"] in remove_ids)]
        merged = kept + new_nodes
        self.http_post_flows(merged)
        self.open_all_tabs(tab_labels)

# d = DisplayMqttTimetable()
# d.run()