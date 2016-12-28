package kr.co.ultari.atsmart.basic.subdata;

public class ChatRoomData {
        public String roomId;
        public String userIds;
        public String userNames;
        public String talkDate;
        public String lastMessage;
        public String read;

        public ChatRoomData( String roomId, String userIds, String userNames, String talkDate, String lastMessage, String read )
        {
                this.roomId = roomId;
                this.userIds = userIds;
                this.userNames = userNames;
                this.talkDate = talkDate;
                this.lastMessage = lastMessage;
                this.read = read;
        }
}
