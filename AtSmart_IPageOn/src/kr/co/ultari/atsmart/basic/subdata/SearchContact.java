package kr.co.ultari.atsmart.basic.subdata;

public class SearchContact {
        private String name;
        private String number;

        public SearchContact( String name, String number )
        {
                this.name = name;
                this.number = number;
        }

        public String getName()
        {
                return name;
        }

        public void setName( String name )
        {
                this.name = name;
        }

        public String getNumber()
        {
                return number;
        }

        public void setNumber( String number )
        {
                this.number = number;
        }
}
