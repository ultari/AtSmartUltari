package kr.co.ultari.atsmart.basic.subdata;

public class NotifyData {
        public String msgId;
        public String receiverId;
        public String senderName;
        public String recvDate;
        public String title;
        public String content;
        public String url;
        public boolean read;

        public NotifyData( String msgId, String receiverId, String senderName, String recvDate, String title, String content, String url, boolean read )
        {
                this.msgId = msgId;
                this.receiverId = receiverId;
                this.senderName = senderName;
                this.recvDate = recvDate;
                this.title = title;
                this.content = content;
                this.url = url;
                this.read = read;
        }
}
