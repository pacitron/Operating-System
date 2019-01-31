import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

/*
This is the hard disk of the program. This s where the data is loaded initially after the input file is read.
The spaces are divided into pages where each page is 8 words long. Each page is then loaded into the main memory
as needed by the CPU.
*/

public class DISK {
    public static String[][] mem = new String[2048][16];
    public static int[] Disk_FMBV=new int[256];
    public static int op_start=0;
    public static int previous=0;

    //constructor where some of the variables are initialized
    public DISK(String add) {
        Loader.p.addrd = conversions.hex2decimal(add);
    }

    //Main function where data is read or written onto the disk divided into pages and words.
    static String memory(String func, String memaddr, String bin) {
        if (func .equals("write")) {
            if (memaddr!=null) {
                int tillwhere=SYSTEM.disk_start+1+SYSTEM.tot_pages;
                for (int i = SYSTEM.disk_start+1; i <= tillwhere; i++) {
                    if (Disk_FMBV[i] == 1) {
                        continue;
                    } else {
                        Disk_FMBV[i] = 1;
                        Loader.p.addrd = 8 * i;
                        break;
                    }
                }
                if(memaddr.equals("ip")){
                    Loader.p.check=true;
                    Loader.p.ip_start=Loader.p.addrd;
                    Loader.p.input_pages++;
                } else if(memaddr.equals("op")){
                    op_start=Loader.p.addrd;
                    Loader.p.output_pages++;
                }
            } else{
                if(Loader.p.counter%8==0 & !Loader.p.check){
                    int tillwhere=SYSTEM.disk_start+1+SYSTEM.tot_pages;
                    for(int i=SYSTEM.disk_start+1;i<=tillwhere;i++){
                        if(Disk_FMBV[i]==1){
                            continue;
                        } else {
                            Disk_FMBV[i]=1;
                            Loader.p.addrd=8*i;
                            Loader.p.total_pages++;
                            if(i==SYSTEM.disk_start+1){
                                Loader.p.pgm_start=Loader.p.addrd;
                            }
                            break;
                        }
                    }
                } else {
                    Loader.p.addrd++;
                }
            }
            Loader.p.count=(Loader.p.addrd-previous);
            Loader.p.map.put(Loader.p.count,Loader.p.addrd);
            Loader.p.counter++;
            StringReader reader = new StringReader(bin);
            int singleChar;
            try {
                int counter = 0;
                    while ((singleChar = reader.read()) != -1) {
                        char ch = (char) singleChar;
                        mem[Loader.p.addrd][counter] = Character.toString(ch);
                        counter++;
                    }

            } catch (IOException e) {
                ERROR_HANDLER.memoryflow();
            } catch (Exception e){
                ERROR_HANDLER.abnormal(e.getMessage());
            }
        } else{
            String temp="";
            int loc= 0;

            if (Loader.p.map.containsKey(Integer.parseInt(memaddr))) {
                loc = Loader.p.map.get(Integer.parseInt(memaddr));
            } else {
                loc=Integer.parseInt(memaddr);
            }
            for(int i=0;i<16;i++){
                temp+=mem[loc][i];
            }
            return temp;
        }
        return "";
    }

    public static void write(int where, String what) {
        try {
            int loc=Loader.p.map.get(where);
            StringReader reader = new StringReader(what);
            int singleChar;
            int counter = 0;
            while ((singleChar = reader.read()) != -1) {
                char ch = (char) singleChar;
                mem[loc][counter] = Character.toString(ch);
                counter++;
            }

        } catch (IOException e) {
            ERROR_HANDLER.memoryflow();
        } catch (Exception e) {
            ERROR_HANDLER.abnormal(e.getMessage());
        }
    }

}
