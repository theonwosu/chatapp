var MessageType = {};

MessageType.MESSAGE = 1;

var senderSessionId = null;
var receiverSessionId = null;

function ChatViewModel(chatObject) {
	var self = this;
	
    var initialize = function () {
    	chatObject.initialize(self.handler);
/*    	
        if (sessionStorage[SESSION_NAME] && sessionStorage[SESSION_NAME].length > 0) {
            self.userName(sessionStorage[SESSION_NAME]);
            self.joined(true);
        }
*/        
    };
    
    self.handler = {      
    		        onclose: function () {
    		            //self.joined(false);
    		        },
    		        onmessage: function (message) {
    		            if (message instanceof Blob) {
    		                message.type = "image/jpg";
    		                var urlCreator = window.URL || window.webkitURL;
    		                var imageUrl = urlCreator.createObjectURL(message);
    		                var img = $('#photo')[0];
    		                img.src=imageUrl;
    		                $('#linkId').attr("href", imageUrl);
    		                self.enableViewImage(true);
    		            } else {
    		                processMessage(JSON.parse(message));
    		            }
    		        }
    		    };  
    
    var processMessage = (function (message) {
    	senderSessionId = message.senderSessionId;
    	receiverSessionId = message.receiverSessionId;
    	//alert(JSON.stringify(message));
    	if (message.type == MessageType.MESSAGE) {
    		addMessageToCollection(message);
    	}
    });
    
    var addMessageToCollection = function (msg) {
        var length = self.messages().length;
        if (length > 0) {
            var prev = self.messages()[length - 1];
            var isFirst = !(prev.userName == msg.userName);
            var isDate = !(new moment(prev.timeSent).startOf("day").isSame(new moment(msg.timeSent).startOf("day")));
            self.messages.push(new DisplayChatMessage(msg.userName, msg.message, msg.timeSent, isFirst, isDate));
        } else {
            self.messages.push(new DisplayChatMessage(msg.userName, msg.message, msg.timeSent, true, true));
        }
    };
    
    function DisplayChatMessage(userName, message, timeSent, isFirst, isDate) {
        var self = this;
        this.type = MessageType.MESSAGE;
        this.userName = userName;
        this.message = message;
        this.isFirst = isFirst;
        this.isDate = isDate;
        this.timeSent = timeSent;

        this.timeSentTimeDisplay = ko.computed(function () {
            return moment(self.timeSent).format('h:mm:ss a');
        });
        this.timeSentDateDisplay = ko.computed(function () {
            return moment(self.timeSent).format('MM/DD/YYYY');
        });
        this.timeSentFullDisplay = ko.computed(function () {
            return moment(self.timeSent).format('MM/DD/YYYY, h:mm:ss a');
        });
    }    
    
    
    initialize();
	
	self.messages = ko.observableArray();
}

$(function(){

    var vm = new ChatViewModel(chat);

    ko.applyBindings(vm);

});

uploadImage = function () {
	if($('#imageId')[0].files[0] == undefined){
		return;
	}
	
	if(receiverSessionId == null || receiverSessionId == senderSessionId){
		alert('You have not established a chat session with an agent')
		return;
	}
    //var input = event.target;
    var reader = new FileReader();
    reader.onload = function () {
       rawData = reader.result;
       chat.sendBinary(rawData);
    };
    
    reader.readAsArrayBuffer($('#imageId')[0].files[0]);
    //reader.readAsArrayBuffer(input.files[0]);
 };
 
 
 
 resetImageField = function() {
 	//alert('resetting');
 	var file = document.getElementById("imageName");
 	file.value = '';
 	
 	
     //var img = $('#photoId')[0];
     //img.src='';
     //$('#linkId').attr("href", '');	
/*	
     var oldInput = document.getElementById('imageId');

     var newInput = document.createElement("input");

     newInput.type = "file";

     newInput.id = oldInput.id;

     newInput.name = oldInput.name;

     newInput.className = oldInput.className;

     newInput.style.cssText = oldInput.style.cssText;

     // TODO: copy any other relevant attributes

     oldInput.parentNode.replaceChild(newInput, oldInput);
*/       
 };
 
 
 $(document).ready(function() {
	  $('#linkId').magnificPopup({type:'image'}); 
	  $( "#viewImageId").hide();
	  $( "#photoId").hide();
});
 
 function ChatMessage(userName, message) {
     this.type = MessageType.MESSAGE;
     this.userName = userName;
     this.message = message;
     this.timeSent = new Date();
     this.receiverSessionId = senderSessionId;
     this.senderSessionId = receiverSessionId;
 }
 
 
 sendMessage = function () {
	 var message = $('#messageId').val();
     var chatMessage = new ChatMessage('Theo', message);
     chat.sendMessage(chatMessage);
 };