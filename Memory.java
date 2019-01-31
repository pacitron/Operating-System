import java.io.IOException;
import java.io.StringReader;
/*
This is the memory module of the system. It contains locations as per the specification to store and
retrieve the instructions. This function is capable of writing,reading and editing(writing) these locations
 */

class Memory {
    public static String[][] mem = new String[256][16];
    public static int[] Memory_FMBV=new int[32];
    //constructor to initialize the starting address

    //the main function where data is read as pages from the disk and also written to the disk as and when
    //necessary. Page replacement algorithms are also implemented in this function if necessary.
    static String memory(String func, String memaddr, String bin) {
        Loader.p.pf=false;
        Loader.p.st="";
        if(memaddr==null){
            Loader.p.current_page=Loader.p.addr/8;
        } else {
            Loader.p.current_page=Integer.parseInt(memaddr)/8;
        }
        Loader.p.where=Loader.p.current_page;
        int temp1=0;
        for(int i=0;i<Loader.smt0.length;i++){
            if(Loader.smt0[i].disk_start==Loader.p.current_page){
                Loader.p.pgm_pointer=i;
                Loader.p.current_page=Loader.smt0[Loader.p.pgm_pointer].mem_start;
                temp1=1;
                break;
            }
        }
        boolean flag=false;
        if(temp1==0){
            for(int i=0;i<Loader.smt0.length;i++){
                if(Loader.smt0[i].disk_start==-1){
                    Loader.p.pgm_pointer=i;
                    Loader.smt0[Loader.p.pgm_pointer].disk_start=Loader.p.current_page;
                    Loader.p.current_page=Loader.smt0[Loader.p.pgm_pointer].mem_start;
                    flag=true;
                    break;
                }
            }
            if(!flag){
                Loader.p.st="replace";
                boolean var=true;
                while(var) {
                    for (int i = Loader.p.replace; i < Loader.smt0.length; i++) {
                        if(Loader.smt0[i].reference==0 && Loader.smt0[i].dirty==0){
                            Loader.p.pgm_pointer=i;
                            Loader.smt0[Loader.p.pgm_pointer].disk_start=Loader.p.current_page;
                            Loader.p.current_page=-1;
                            var=false;
                            Loader.p.replace=i;
                            break;
                        }
                    }
                    if(var==true){
                        for (int i = 0; i < Loader.smt0.length; i++) {
                            if (Loader.smt0[i].reference == 0 && Loader.smt0[i].dirty == 1) {
                                Loader.p.pgm_pointer = i;
                                Loader.smt0[Loader.p.pgm_pointer].disk_start = Loader.p.current_page;
                                Loader.p.current_page=-1;
                                var = false;
                                break;
                            }
                        }
                    }
                    if(var==true){
                        for (int i = 0; i < Loader.smt0.length; i++) {
                            Loader.smt0[i].reference=0;
                        }
                    }
                }
            }
        }
        if(Loader.p.current_page==-1){
            Loader.p.current_page=Loader.smt0[Loader.p.pgm_pointer].mem_start;
            Loader.p.clock+=10;
            Loader.p.clbreak+=10;
            Loader.p.memclock+=10;
            Loader.p.smt0=Loader.smt0;
            Loader.p.status=Loader.p.st;
            Loader.p.current_page1=Loader.p.current_page;
            Page_fault_handler(Loader.p.st, Loader.smt0[Loader.p.pgm_pointer]);
            Loader.p.pf=true;
        } else {
            if (Memory_FMBV[Loader.p.current_page] != 1) {
                Loader.p.pgm_pointer++;
                Loader.smt0[Loader.p.pgm_pointer].disk_start = Loader.p.current_page;
                Loader.p.clock+=10;
                Loader.p.clbreak+=10;
                Loader.p.memclock+=10;
                Loader.p.smt0=Loader.smt0;
                Loader.p.status=Loader.p.st;
                Loader.p.current_page1=Loader.p.current_page;
                Page_fault_handler(Loader.p.st, Loader.smt0[Loader.p.pgm_pointer]);
                Loader.p.pf=true;
            }
        }

        if(Loader.p.clock-Loader.p.clocktemp>=15){
            Loader.p.clocktemp=15*(++Loader.p.time);
            Loader.p.PMTprint+="PMT for "+Loader.p.time*15+"VTU:\nPage number  Frame number\n";
            for(int i=0;i<Loader.smt0.length;i++){
                if(Loader.smt0[i].disk_start!=-1) {
                    Loader.p.PMTprint+=Loader.smt0[i].disk_start + "   \t\t  " + Loader.smt0[i].mem_start+"\n";
                }
            }
            Loader.p.PMTprint+="------------------\n";
        }
        Loader.p.actual_location=(Loader.p.current_page*8)+(Integer.parseInt(memaddr)%8);
        if (func.equals("write")) {
            Loader.smt0[Loader.p.pgm_pointer].dirty=1;
            StringReader reader = new StringReader(bin);
            int singleChar;
            try {
                int counter = 0;
                while ((singleChar = reader.read()) != -1) {
                    char ch = (char) singleChar;
                    mem[Loader.p.actual_location][counter] = Character.toString(ch);
                    counter++;
                }
                Loader.p.addr++;

            } catch (IOException e) {
                ERROR_HANDLER.memoryflow();
            } catch (Exception e){
                ERROR_HANDLER.abnormal(e.getMessage());
            }
        } else{
            Loader.smt0[Loader.p.pgm_pointer].reference=1;
            String temp="";
            for(int i=0;i<16;i++){
                temp+=mem[Loader.p.actual_location][i];
            }
            return temp;
        }
        return "";
    }
    static String get(int add){
        String temp="";
        for(int i=0;i<16;i++){
            temp+=mem[add][i];
        }
        return temp;
    }
    static void write(int where,String what) {
        StringReader reader = new StringReader(what);
        int singleChar;
        try {
            int counter = 0;
            while ((singleChar = reader.read()) != -1) {
                char ch = (char) singleChar;
                mem[where][counter] = Character.toString(ch);
                counter++;
            }
        } catch (IOException e) {
            ERROR_HANDLER.memoryflow();
        } catch (ArrayIndexOutOfBoundsException e) {
            ERROR_HANDLER.PCerror();
        } catch (Exception e){
            ERROR_HANDLER.abnormal(e.getMessage());
        }
    }

    //This function handles all the page faulta that may occur when the CPU tries to read from a page and
    // it is not present. This adds to a free space on the memory or replaces the page.
    static void Page_fault_handler(String st,PMT smt){
        Loader.p.pagefaults++;
        int ip=0;
        if(st.equals("replace")){
            for(int i=0;i<8;i++){
                String rd=DISK.memory("read",Integer.toString((Loader.p.where*8)+i),"");
                if(rd.length()>16){
                    Loader.p.unused++;
                    continue;
                }
                write(((Loader.p.current_page*8)+i),rd);
            }
            Loader.p.current_page=smt.mem_start;
        } else {
            for (int i = 0; i < Loader.p.allocated_frames.length; i++) {
                if (Memory_FMBV[Loader.p.allocated_frames[i]] == 0) {
                    Memory_FMBV[Loader.p.allocated_frames[i]] = 1;
                    smt.mem_start = Loader.p.allocated_frames[i];
                    Loader.p.current_page = Loader.p.allocated_frames[i];
                    for (int j = 0; j < 8; j++) {
                        String p = Integer.toString(j + (smt.disk_start * 8));
                        String p1 = Integer.toString(j + (smt.mem_start * 8));
                        String pp = DISK.memory("read", p, null);
                        if(pp.length()>16){
                            Loader.p.unused++;
                            continue;
                        }
                        String temp=memory("write", p, pp);

                    }
                    break;
                }
            }
        }
    }

}
