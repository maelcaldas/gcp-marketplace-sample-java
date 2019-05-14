package com.bkper.billing.google.pubsub;

/**
 * A message captures information from the Pubsub message received over the push endpoint
 */
public class PubsubMessage {
  private String messageId;
  private String publishTime;
  private String data;

  public PubsubMessage(String messageId) {
    this.messageId = messageId;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getPublishTime() {
    return publishTime;
  }

  public void setPublishTime(String publishTime) {
    this.publishTime = publishTime;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

@Override
public String toString() {
    return "PubsubMessage [messageId=" + messageId + ", publishTime=" + publishTime + ", data=" + data + "]";
}
  
  
}
