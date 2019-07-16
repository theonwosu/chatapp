package gov.ssa;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import gov.ssa.coders.MessageDecoder;
import gov.ssa.coders.MessageEncoder;
import gov.ssa.models.ChatMessage;
import gov.ssa.models.Message;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@ServerEndpoint(value = "/websocket/chat", decoders=MessageDecoder.class, encoders=MessageEncoder.class)
public class ChatServerEndpoint {

    private static List<Session> clients = new CopyOnWriteArrayList<Session>();
    private static Map<String, Session> sessionMap = new HashMap<>();
    Session session;

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    	System.out.println("Opened session.....");
        this.session = session;
        clients.add(session);
        sessionMap.put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("socket closed: " + reason.getReasonPhrase());
        clients.remove(session);
    }


    ByteArrayOutputStream buffer = new ByteArrayOutputStream();


    @OnMessage
    public void onMessage(ByteBuffer byteBuffer, boolean complete) {
        try {
            buffer.write(byteBuffer.array());
            if (complete) {
/*
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream("e:\\delete\\image.jpg");
                    fos.write(buffer.toByteArray());
                } finally {
                    if (fos != null) {
                        fos.flush();
                        fos.close();
                    }
                }
                
                
                
*/          	            
            	
                for (Session client : clients) {
                    final ByteBuffer sendData = ByteBuffer.allocate(buffer.toByteArray().length);
                    sendData.put(buffer.toByteArray());
                    sendData.rewind();
                    client.getAsyncRemote().sendBinary(sendData, new SendHandler() {
                        @Override
                        public void onResult(SendResult sendResult) {
                            //System.out.println(sendResult.isOK());
                        }
                    });
                } 
                buffer = new ByteArrayOutputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(Message message) {
    	System.out.println("Session Id: "+session.getId());
        if (message instanceof ChatMessage) {
            processMessage((ChatMessage) message);
        } 
        else {
            System.out.println("Unknown message");
        }
    }    
    
    private void processMessage(ChatMessage message) {
    	String senderSessionId = message.getSenderSessionId();
    	String receiverSessionId = message.getReceiverSessionId();
    	if(senderSessionId == null) {
    		broadcast(message);
    	}    	
    	else if(senderSessionId != null && senderSessionId.equals(receiverSessionId)) {
    		//broadcast message because sender has been sending messages without any response.
    		broadcast(message);
    	}
    	else {
    		Session client = sessionMap.get(receiverSessionId); 

            try {
        		//Send private message to receiver
                client.getBasicRemote().sendObject(message);
            } 
            catch (IOException e) {
                clients.remove(client);
                try {
                    client.close();
                } catch (IOException e1) {
                    // do nothing
                }
            } catch (EncodeException e) {
                e.printStackTrace();
            }
            
            
            try {

        		//Send a copy to sender
        		message.setReceiverSessionId(message.getSenderSessionId());                
                session.getBasicRemote().sendObject(message);
            } 
            catch (IOException e) {
                clients.remove(session);
                try {
                	session.close();
                } catch (IOException e1) {
                    // do nothing
                }
            } catch (EncodeException e) {
                e.printStackTrace();
            }            
    	}
        
    } 
    
    private void broadcast(Message message) {
        for (Session client : clients) {
        	message.setReceiverSessionId(client.getId());
        	message.setSenderSessionId(session.getId());
            try {
                client.getBasicRemote().sendObject(message);
            } 
            catch (IOException e) {
                clients.remove(client);
                try {
                    client.close();
                } catch (IOException e1) {
                    // do nothing
                }
            } catch (EncodeException e) {
                e.printStackTrace();
            }
        }
    }    
}
