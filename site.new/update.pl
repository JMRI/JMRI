#!/usr/bin/perl

use DirHandle;

$DEBUG=0;
$dir=".";	# start in current directory...

# $docroot="/trains/JMRI/site.new";
$docroot="/";
$logoalt = "\"JMRI Logo\"";
# absolute path from docroot, without the leading slash...
$logo = "images/logo-jmri.gif";

$TDIR="./templates";

$H = "Header";
$F = "Footer";
$S = "Style";
$L = "Logo";


sub processfile() {
	my ($f) = @_;
	my $old = $f;
	my $new = $f;

	$old =~ s/html$/html.bak/;
	$new =~ s/html$/html.new/;

	die "can not create unique name for $f (old=$old, new=$new)\n"
		if ($new eq $old || $new eq $f || $old eq $f);

	my $regen=0;
	foreach my $x ("$TDIR/$H", "$TDIR/$F", "$TDIR/$S", "$TDIR/$L") {
		printf ("%25s %8.6f\t%25s %8.6f\n", $x, (-M $x), $f, (-M $f))
			if ($DEBUG);
		if (-M $x < -M $f) {
			print "Regenerating $f because $x is newer\n"
				if ($DEBUG);
			$regen=1;
			last;
		}
	}
	if ($regen == 1) {
	    print "Processing $f ...";

	    # Reset
	    my $state="copy";
	    my @boilerplate = ();
	    my %defines=undef;
	    # set up default values
	    $defines{"docroot"} = $docroot;
	    $defines{"logo"}    = $logo;
	    $defines{"logoalt"} = $logoalt;


	    open(OLD, "< $f")         or die "can't open $old: $!";
	    open(NEW, "> $new")         or die "can't open $new: $!";
	    while (<OLD>) {
		if ($state eq "copy") {
		    #<!-- Define tag value -->
		    if ( /^<!-- Define ([a-zA-Z0-9_-]+)\s(.*) -->/ ) {
			    $defines{$1} = $2;
			    print NEW $_;
		    } elsif (/^<!-- Header -->/) {
		    	    print("[Header]");
			    $lookfor="<!-- \/Header -->";
			    $state="delete";
			    @boilerplate=@HC;
		    } elsif (/^<!-- Footer -->/) {
		    	    print("[Footer]");
			    $lookfor="<!-- \/Footer -->";
			    $state="delete";
			    @boilerplate=@FC;
		    } elsif (/<!-- Logo -->/) {
		    	    print("[Logo]");
			    $lookfor="<!-- \/Logo -->";
			    $state="delete";
			    @boilerplate=@LC;
		    } elsif (/<!-- Style -->/) {
		    	    print("[Style]");
			    $lookfor="<!-- \/Style -->";
			    $state="delete";
			    @boilerplate=@SC;
		    } else {
			    print NEW $_;
		    }
		} elsif ($state eq "delete") {
		    if (/$lookfor/) {
			# grab a copy of the boilerplate
			my $c = join "", @boilerplate;

			# and substitute all the ${tags} found in it
			# (undefined tags are not changed)
			foreach $tag (keys %defines) {
			    my $val = $defines{$tag};
			    $c =~ s|\${$tag}|$val|g;
			}
			print NEW $c;
			$state="copy";
		    }
		}
	    }
	    close(OLD)                  or die "can't close $old: $!";
	    close(NEW)                  or die "can't close $new: $!";
	    print("[backup]");
	    rename($f, $old)   or die "can't rename $f to $old: $!";
	    print("[rename]");
	    rename($new, $f)   or die "can't rename $new to $f: $!";
	    printf(" Done\n");
	}
}




sub getfilenames() {
	my ($d) = @_;
	my @f = ();
	my $file;
	my $dh = DirHandle->new($d)   or die "Can't open $d : $!\n";

	while( defined($file = $dh->read()) ) {
	    my $filename = "$d/$file";
	    next if (($file =~ /^\.$/) || ($file =~ /^\.\.$/));
	    if (-d $filename) {
	    	push (@f, &getfilenames($filename));
	    } elsif ($file =~ /\.html$/i) {
		push(@f, $filename);
	    }
	    # else Ignored 
	}
	return @f;
}


open(F, "< $TDIR/$H") or die "can't open $H: $!"; @HC = <F>; close(F); 
open(F, "< $TDIR/$F") or die "can't open $F: $!"; @FC = <F>; close(F); 
open(F, "< $TDIR/$S") or die "can't open $S: $!"; @SC = <F>; close(F); 
open(F, "< $TDIR/$L") or die "can't open $L: $!"; @LC = <F>; close(F);


# @files = ( "index.html" );
@files = &getfilenames($dir);

foreach $f (@files) {
	&processfile($f);
}

