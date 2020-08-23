class Logger {

   log(message){
       if(console && console.log){
           console.log(message);
       }
   }

   warn(message){
       if(console && console.warn){
           console.warn(message);
       } else {
           this.log(message);
       }
   }

   error(message){
       if(console && console.error){
           console.error(message);
       } else {
           this.warn(message);
       }
   }
}
