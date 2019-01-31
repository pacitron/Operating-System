/*
This class is used for all the conversions of bases that may be needed
such as binary to decimal, hexa-decimal to binary, etc
 */

class conversions {

    //this function converts hexa to binary
    static String hexToBin(String hex) {
        String bin = "";
        String part = "";
        int iHex;
        hex = hex.trim();
        hex = hex.replaceFirst("0x", "");
        for (int i = 0; i < hex.length(); i++) {
            iHex = Integer.parseInt("" + hex.charAt(i), 16);
            part = Integer.toBinaryString(iHex);
            while (part.length() < 4) {
                part = "0" + part;
            }
            bin += part;
        }
        return bin;
    }

    //this function converts from hexa to decimal
     public static int hex2decimal(String s) {
         String digits = "0123456789ABCDEF";
         s = s.toUpperCase();
         int val = 0;
         for (int i = 0; i < s.length(); i++) {
             char c = s.charAt(i);
             int d = digits.indexOf(c);
             val = 16*val + d;
         }
         return val;
     }

     //this function converts from decimal to binary
     public static String dectobin(int i){
         String binarized = Integer.toBinaryString(i);
         int len = binarized.length();
         String sixteenZeroes = "00000000000000000";
         if (len < 16)
             binarized = sixteenZeroes.substring(0,(16-len)).concat(binarized);
         else
             binarized = binarized.substring((len-16));
         return binarized;
     }

     //this function converts from binary to decimal
     public static int bintodec(String binaryString){
             if (binaryString.charAt(0) == '1') {
                 String invertedInt = invert(binaryString);
                 int decimalValue = Integer.parseInt(invertedInt, 2);
                 decimalValue = (decimalValue + 1) * -1;
                 return decimalValue;
             } else {
                 return Integer.parseInt(binaryString, 2);
             }
         }
    //this function inverts binary digits
     public static String invert(String binaryInt) {
         String result = binaryInt;
         result = result.replace("0", " ");
         result = result.replace("1", "0");
         result = result.replace(" ", "1");
         return result;
     }

 }
