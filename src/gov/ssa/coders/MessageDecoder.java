package gov.ssa.coders;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ssa.models.ChatMessage;
import gov.ssa.models.Message;


public class MessageDecoder implements Decoder.Text<Message> {
    static final int CHAT_MESSAGE = 1;
    
    @Override
    public Message decode(String msg) throws DecodeException {
        Message message = null;

        if(willDecode(msg)){
            try {
                JsonObject obj = Json.createReader(new StringReader(msg)).readObject();
                ObjectMapper mapper = new ObjectMapper();

                int type = obj.getInt("type");

                switch (type) {
                    case CHAT_MESSAGE:
                        message = mapper.readValue(msg, ChatMessage.class);
                        break;
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        return message;
    }

    @Override
    public boolean willDecode(String msg) {
        try {
            Json.createReader((new StringReader(msg)));
            return true;
        } catch (JsonException e){
            return false;
        }
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
