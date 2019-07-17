package gov.ssa.models;

public class Message {
    private int type;
    private String senderSessionId;
    private String receiverSessionId;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

	public String getSenderSessionId() {
		return senderSessionId;
	}

	public void setSenderSessionId(String senderSessionId) {
		this.senderSessionId = senderSessionId;
	}

	public String getReceiverSessionId() {
		return receiverSessionId;
	}

	public void setReceiverSessionId(String receiverSessionId) {
		this.receiverSessionId = receiverSessionId;
	} 
}
