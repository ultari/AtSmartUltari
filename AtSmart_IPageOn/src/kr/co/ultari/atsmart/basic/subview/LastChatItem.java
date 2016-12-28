package kr.co.ultari.atsmart.basic.subview;

public class LastChatItem {
        public static String m_chatId = "";
        public static String m_roomId = "";
        public static String m_talkerId = "";
        public static String m_talkerName = "";
        public static String m_talkerNickName = "";
        public static String m_talkDate = "";
        public static String m_talkerContent = "";
        public static String m_unReadUserIds = "";
        public static String m_isSendComplete = "";
        public static String m_isReserved = "";

        public static void setData( String chatId, String roomId, String talkerId, String talkerName, String nick, String date, String content,
                        String unReadUserIds, boolean sendComplete, boolean reserved )
        {
                m_chatId = chatId;
                m_roomId = roomId;
                m_talkerId = talkerId;
                m_talkerName = talkerName;
                m_talkerNickName = nick;
                m_talkDate = date;
                m_talkerContent = content;
                m_unReadUserIds = unReadUserIds;
                if ( sendComplete ) m_isSendComplete = "Y";
                else m_isSendComplete = "N";
                if ( reserved ) m_isReserved = "Y";
                else m_isReserved = "N";
        }
}
