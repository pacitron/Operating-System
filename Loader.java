import java.io.*;

/*
This class/module is used to load all the input file content into the memory. This class is called from the
System class. This class just loads the first page to the memory and initializes the PCB and other process
relevant variables.
 */
class Loader {
    public static int start;
    public static int no_frames;
    public static int[] reserve=new int[32];
    public static PMT[] smt0;
    public static PCB p;
    // this function loads the instructions into the memory
    public static void load(String startaddr, String TF) {
        try {
            p=new PCB();
            smt0=p.smt0;
            p.jid=SYSTEM.jid;
            p.injid=SYSTEM.injid;
            p.inp_pointer=Loader.p.ip_start;
            p.op_pointer=DISK.op_start;
            p.pgm_pointer=Loader.p.pgm_start;
            start=Integer.parseInt(startaddr);
            no_frames=Math.min(6,Loader.p.total_pages+2);
            //initial page load...
            int count=1;
            for(int i=0;i<reserve.length;i++){
                if(reserve[i]==0){
                    reserve[i]=1;
                    p.allocated_frames[count-1]=i;
                } else {
                    continue;
                }
                count++;
                if(count>6){break;}
            }
            for(int i=0;i<6;i++){
                smt0[i]=new PMT();
            }

            Memory.Memory_FMBV[p.allocated_frames[0]]=1;
            smt0[0].disk_start=SYSTEM.PC/8;
            smt0[0].mem_start=p.allocated_frames[0];
            smt0[0].valid=1;
            Loader.p.clock+=15;
            load();

        } catch (NumberFormatException e) {
            ERROR_HANDLER.improperinput();
        } catch (Exception e){
            ERROR_HANDLER.abnormal(e.getMessage());
        }
    }

    //loads a given frame
    static void load(){
        for(int i=smt0[0].disk_start*8;i<((smt0[0].disk_start*8)+8);i++){
            String load = DISK.memory("read",Integer.toString(i),"");
            Memory.memory("write",Integer.toString(i),load);
        }
    }

    public static void reinitialize(){
         start=0;
         no_frames=0;
         reserve=new int[32];
    }

}
