package kr.co.ultari.atsmart.basic;

public class EnumDefine {
        // Company
        private enum Project
        {
                BASIC, SAMSUNG, CU, ENERGY, CNUH, SAMYANG, KORAIL, REDCROSS, MOORIM, MODETOUR, POLICE, NEC, UST, NONGSHIM, KEAD
        }
        private static final Project SET_COMPANY = Project.BASIC;

        public Project getCompany()
        {
                return SET_COMPANY;
        }
        // LOGIN
        private enum LoginMode
        {
                ID, NAME
        }
        private static final LoginMode LOGIN_MODE = LoginMode.ID;

        public LoginMode getLoginMode()
        {
                return LOGIN_MODE;
        }
        // BACKGROUND
        private enum BackgroundMode
        {
                IMAGE, COLOR
        }
        private static final BackgroundMode SELECT_BACKGROUND_MODE = BackgroundMode.IMAGE;

        public BackgroundMode getBackgroundMode()
        {
                return SELECT_BACKGROUND_MODE;
        }
}
