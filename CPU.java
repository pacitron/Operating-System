import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Scanner;

/*
This is the class where the instructions are decoded and executed. This class also contains the instruction register,
base register, the stack etc. This is where the instructions are continuously fetched from the memory and executed
until a halt is given.
 */

public class CPU {

    @SuppressWarnings("unchecked")
    public static String main(String pc, String TF){
        if(TF.equals("1")){
            Loader.p.traceflag=true;
        }
        Loader.p.PC=conversions.bintodec(Loader.p.BR)+conversions.hex2decimal(pc);
        Loader.p.trace=TF;
        String temp=Decode();
        Loader.p.PC1=Loader.p.PC;
        store obj=new store();
        int t1=Integer.parseInt(Loader.p.injid)-1;
        SYSTEM.obmap.put(t1,obj);
        return temp;
    }

    //this function continuously fetches the instruction from the memory according to the program counter (PC).
    // The instruction is then decoded here accordingly and sent to execute() function. Necessary writes are
    // made for the trace file log if bit enabled.

    @SuppressWarnings("unchecked")
    public static String Decode(){
        try {
            String locname="trace_file"+Integer.toString(SYSTEM.internalid)+".txt";
            Loader.p.file=new File(locname);
            Loader.p.outputWriter = new BufferedWriter(new FileWriter(Loader.p.file));
            if (Loader.p.traceflag) {
                Loader.p.outputWriter.write("\t BEFORE EXECUTION(ALL IN HEX) AFTER EXECUTION(ALL IN HEX)\nPC\tBR\tIR" +
                        "\t\tTOS\t S(TOS)\t EA  " + "(EA) TOS  S(TOS)  EA\t(EA)\n");
                Loader.p.outputWriter.flush();
            }

            // continuous instruction fetch
        while (true){
            Loader.p.list=new ArrayList();
            String temp="";

            String inst=Memory.memory("read",Integer.toString(Loader.p.PC),null);
            if(Loader.p.pf){
                return "pf";
            }
            Loader.p.output_content+=Integer.toHexString(Loader.p.PC)+"\t";
            StringReader reader = new StringReader(inst);
            int singleChar;
            String[] in=new String[16];
            int counter=0;
            while ((singleChar = reader.read()) != -1){
                char ch = (char) singleChar;
                in[counter]= String.valueOf(ch);
                counter++;
            }
            String[] getins = in;
            Loader.p.PC++;
            Loader.p.IR=getins;
            Loader.p.output_content+=Integer.toHexString(conversions.bintodec(Loader.p.BR));
            Loader.p.output_content+="\t";
            String ir="";
            for (String i:Loader.p.IR
                 ) {
                ir+=i;
            }
            String hex=Integer.toHexString(conversions.bintodec(ir));
            if(hex.length()>=4) {
                Loader.p.output_content+=hex.substring(hex.length()-4);
            } else {
                Loader.p.output_content+=hex+"\t";
            }
            if(Loader.p.EA==0){
                Loader.p.EAins="000000000000000";
            } else {
                Loader.p.EAins = Memory.memory("read", Integer.toString(Loader.p.EA), "");

            }
            printbefore();
            //check for 0 or 1 address instruction done here
            if(getins[0].equals("0")){
                Loader.p.type=0;
                for(int i=3;i<getins.length/2;i++){
                    temp+=getins[i];
                }
                Loader.p.instruction=temp;
                Loader.p.clock++;
                Loader.p.CPUtime++;
                String t1=execute();
                if(t1.equals("END")){
                    return t1;
                }
                temp="";
                for(int j=(getins.length/2)+3;j<getins.length;j++){
                    temp+=getins[j];
                }
                Loader.p.instruction=temp;
                Loader.p.clock++;
                Loader.p.CPUtime++;
                t1=execute();
                if(t1.equals("END")){
                    return t1;
                }
            } else{
                Loader.p.type=1;
                temp="";
                for(int k=1;k<6;k++){
                    temp+=getins[k];
                }
                Loader.p.instruction=temp;
                temp="";
                for(int k=9;k<getins.length;k++){
                    temp+=getins[k];
                }
                if(getins[6]=="1"){
                    Loader.p.EA=Integer.parseInt(temp,2)+conversions.bintodec(Loader.p.Stack[Loader.p.TOS])+conversions.bintodec(Loader.p.BR);
                } else{
                    Loader.p.EA=Integer.parseInt(temp,2)+conversions.bintodec(Loader.p.BR);
                }
                Loader.p.clock=Loader.p.clock+4;
                Loader.p.CPUtime+=4;
                Loader.p.EAins=Memory.memory("read", Integer.toString(Loader.p.EA), "");

                String t1=execute();
                if(t1.equals("END")){
                    return t1;
                }

            }

            //code for trace file logs.
            printafter();
            //error handling code
            if(Loader.p.pf){
                return "pf";
            }
            if(Loader.p.clock>=Loader.p.clbreak){
                Loader.p.clbreak+=20;
                return "pf";
            }
            if(Loader.p.clock>10000){
                ERROR_HANDLER.infinite();
            }
            if(Loader.p.EA-conversions.bintodec(Loader.p.BR)>128){
                ERROR_HANDLER.memoryflow();
            }
        }

        } catch (IOException e) {
            ERROR_HANDLER.improperinput();
        } catch (IllegalArgumentException e) {
            ERROR_HANDLER.dividezero();
        } catch (NullPointerException e){
            ERROR_HANDLER.stackerror();
        } catch (ArrayIndexOutOfBoundsException e){
            ERROR_HANDLER.stackover();
        } catch (Exception e){
            ERROR_HANDLER.abnormal(e.getMessage());
        }
        return "";
    }
    @SuppressWarnings("unchecked")
    public static void printbefore(){
        if(Loader.p.Stack[Loader.p.TOS]==null){
            Loader.p.list.add(Integer.toHexString(Loader.p.TOS));
            Loader.p.list.add(Integer.toHexString(0));
            Loader.p.list.add(Integer.toHexString(Loader.p.EA));
            if(Loader.p.EAins.length()>16){
                Loader.p.list.add(0);
            } else {
                Loader.p.list.add(Integer.toHexString(Integer.parseInt(Loader.p.EAins, 2)));
            }
        } else {
            Loader.p.list.add(Integer.toHexString(Loader.p.TOS));
            Loader.p.list.add(Integer.toHexString(Integer.parseInt(Loader.p.Stack[Loader.p.TOS],2)));
            Loader.p.list.add(Integer.toHexString(Loader.p.EA));
            if(Loader.p.EAins.length()>16){
                Loader.p.list.add(0);
            } else {
                Loader.p.list.add(Integer.toHexString(Integer.parseInt(Loader.p.EAins, 2)));
            }
        }
    }
    @SuppressWarnings("unchecked")
    public static void printafter(){
        if(Loader.p.Stack[Loader.p.TOS]==null){
            Loader.p.list.add(Integer.toHexString(Loader.p.TOS));
            Loader.p.list.add(Integer.toHexString(0));
            Loader.p.list.add(Integer.toHexString(Loader.p.EA));
            if(Loader.p.EAins.length()>16){
                Loader.p.list.add(0);
            } else {
                Loader.p.list.add(Integer.toHexString(Integer.parseInt(Loader.p.EAins, 2)));
            }
        } else {
            Loader.p.list.add(Integer.toHexString(Loader.p.TOS));
            Loader.p.list.add(Integer.toHexString(Integer.parseInt(Loader.p.Stack[Loader.p.TOS],2)));
            Loader.p.list.add(Integer.toHexString(Loader.p.EA));
            if(Loader.p.EAins.length()>16){
                Loader.p.list.add(0);
            } else {
                Loader.p.list.add(Integer.toHexString(Integer.parseInt(Loader.p.EAins, 2)));
            }
        }
        ListIterator it=Loader.p.list.listIterator();
        while (it.hasNext()) {
            Loader.p.output_content+=String.format("%6s", it.next());
        }
        Loader.p.output_content+="\n";
        Loader.p.PMTprint="";
            Loader.p.clocktemp=15*(++Loader.p.time);
            Loader.p.PMTprint+="PMT "+"\nPage number  Frame number\n";
            for(int i=0;i<Loader.p.smt0.length;i++){
                if(Loader.p.smt0[i].disk_start!=-1) {
                    Loader.p.PMTprint+=Loader.p.smt0[i].disk_start + "    \t\t " + Loader.p.smt0[i].mem_start+"\n";
                }
            }
            for(int i=0;i<Loader.p.smt0.length;i++){
                if(Loader.p.smt0[i].disk_start==Loader.p.ip_start/8) {
                    Loader.p.PMTprint+="INPUT\n";
                    Loader.p.PMTprint+=Loader.p.smt0[i].disk_start + "    \t\t " + Loader.p.smt0[i].mem_start+"\n";
                }
            }
            for(int i=0;i<Loader.p.smt0.length;i++){
                if(Loader.p.smt0[i].disk_start==Loader.p.output_start/8) {
                    Loader.p.PMTprint+="OUTPUT\n";
                    Loader.p.PMTprint+=Loader.p.smt0[i].disk_start + "    \t\t " + Loader.p.smt0[i].mem_start+"\n";
                }
            }
            Loader.p.PMTprint+="------------------\n";
    }

    //OUTPUT spooling is done here where all the necessary statistics are calculates and all the necessary information
    // is written to a file.
    @SuppressWarnings("unchecked")
    public static void outputfile(){
        StringBuilder output=new StringBuilder();
        if(SYSTEM.terror){
            SYSTEM.terror=false;
            Loader.p.warnings="BAD TRACE FLAG";
        }
        SYSTEM.cputime.add(Loader.p.CPUtime);
        SYSTEM.inp_segment.add(Loader.p.offset);
        SYSTEM.op_segment.add(Loader.p.write-Loader.p.output_start);
        SYSTEM.io_requests.add((Loader.p.write-Loader.p.output_start)+Loader.p.offset);
        SYSTEM.meanfaults.add(Loader.p.pagefaults);
        try {
            if (Loader.p.traceflag) {
                Loader.p.outputWriter.write(Loader.p.output_content);
                Loader.p.outputWriter.flush();
            }


            output.append(Loader.p.PMTprint);
            output.append("Job Identification number(DEC): "+SYSTEM.internalid);
            if(Loader.p.warnings!="none") {
                output.append("\nWarning messages: " + Loader.p.warnings);
                SYSTEM.errjobs++;
            }
        if(Loader.p.flag){
            Loader.p.nature="abnormal\n"+Loader.p.errors;
        } else{
            Loader.p.nature+=Loader.p.errors;
        }
        output.append("\nnature of termination: "+Loader.p.nature);
        Loader.p.warnings="";
        Loader.p.nature="normal";
        Loader.p.errors="";
        output.append("\ninput segment:\n"+Loader.p.ids);
        if(Loader.p.output.equals("")){
            output.append("output segment:\nNO OUTPUT GENERATED\n");
        } else {
            output.append("output segment:\n" +Loader.p. output);
        }
        output.append("clock value(HEX):"+Integer.toHexString(Loader.p.clock));
        output.append("\nruntime(DECIMAL):"+Loader.p.clock+"\nExecution time(DECIMAL):"
                +(Loader.p.clock-Loader.p.io-Loader.p.memclock)+"\ninput/output time(DECIMAL):"+Loader.p.io);
        output.append("\nPage fault time(DEC) :"+Loader.p.pagefaults*10);
        output.append("\nSegment fault time(DEC) :"+Loader.p.segfaults);
        DecimalFormat df = new DecimalFormat("#.##");
        double mword=(((Loader.no_frames*8)-Loader.p.unused)/256.0)*100;
        SYSTEM.memutil.add(mword);
        double mpage=(Loader.no_frames/32.0)*100;
        double mwratio=(256.0/((Loader.no_frames*8)-Loader.p.unused));
        double mpratio=(32.0/Loader.no_frames);
        output.append("\nMemory utilization:\nWords:- Percentage: "+ df.format(mword) +"% Ratio: 1:"+df.format(mwratio)+
                "\nPages:- Percentage: "+ df.format(mpage) +"% Ratio: 1:"+df.format(mpratio));
        SYSTEM.code_segment.add((((Loader.p.total_pages+Loader.p.input_pages+Loader.p.output_pages)*8)-SYSTEM.diskunused));
        double dword=((((Loader.p.total_pages+Loader.p.input_pages+Loader.p.output_pages)*8)-SYSTEM.diskunused)/2048.0)*100;
        SYSTEM.diskutil.add(dword);
        double dpage=((Loader.p.total_pages+Loader.p.input_pages+Loader.p.output_pages)/256.0)*100;
        double dwratio=(2048.0/(((Loader.p.total_pages+Loader.p.input_pages+Loader.p.output_pages)*8)-SYSTEM.diskunused));
        double dpratio=(256.0/(Loader.p.total_pages+Loader.p.input_pages+Loader.p.output_pages));
        output.append("\nDisk utilization:\nWords:- Percentage: "+ df.format(dword) +"% Ratio: 1:"+df.format(dwratio)+
                "\nPages:- Percentage: "+ df.format(dpage) +"% Ratio: 1:"+df.format(dpratio));
        double diskfrag=-3;
            for(int i=0;i<8;i++){
                if(DISK.mem[(Loader.p.total_pages*8)+i][0]==null){
                    diskfrag++;
                }
                if(DISK.mem[(Loader.p.ip_start)+i][0]==null){
                    diskfrag++;
                }
                if(DISK.mem[(DISK.op_start)+i][0]==null){
                    diskfrag++;
                }
            }
            diskfrag/=3;
        SYSTEM.opt.put(SYSTEM.internalid,output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //This function gets the opcode from the decode() function and puts it in a switch case to branch it to the
    // required operation that needs to be performed.
    @SuppressWarnings("unchecked")
    public static String execute() {
        if(Loader.p.type==0){
            switch (Loader.p.instruction){
                case "00000":break;
                case "00001":OR();
                    break;
                case "00010":AND();
                    break;
                case "00011":NOT();
                    break;
                case "00100":XOR();
                    break;
                case "00101":ADD();
                    break;
                case "00110":SUB();
                    break;
                case "00111":MUL();
                    break;
                case "01000":DIV();
                    break;
                case "01001":MOD();
                    break;
                case "01010":SL();
                    break;
                case "01011":SR();
                    break;
                case "01100":CPG();
                    break;
                case "01101":CPL();
                    break;
                case "01110":CPE();
                    break;
                case "01111":break;
                case "10000":break;
                case "10001":break;
                case "10010":break;
                case "10011":
                    Loader.p.clock=Loader.p.clock+15;
                    Loader.p.clbreak+=15;
                    Loader.p.io+=15;
                    String temp=RD();
                    return temp;
                case "10100":
                    Loader.p.clock=Loader.p.clock+15;
                    Loader.p.clbreak+=15;
                    Loader.p.io+=15;
                    String temp1=WR();
                    return temp1;
                case "10101":RET();
                    break;
                case "10110":break;
                case "10111":break;
                case "11000":

                    outputfile();
                    try {
                        if(Loader.p.trace.equals("1")){
                            Loader.p.outputWriter.close();
                        } else {
                            Loader.p. file.delete();
                        }
                    } catch (IOException e) {
                        ERROR_HANDLER.fileerror();
                    }
                    return "END";
            }
    } else{
            switch (Loader.p.instruction){
                case "00000":break;
                case "00001":OR1();
                    break;
                case "00010":AND1();
                    break;
                case "00011":break;
                case "00100":XOR1();
                    break;
                case "00101":ADD1();
                    break;
                case "00110":SUB1();
                    break;
                case "00111":MUL1();
                    break;
                case "01000":DIV1();
                    break;
                case "01001":MOD1();
                    break;
                case "01010":break;
                case "01011":break;
                case "01100":CPG1();
                    break;
                case "01101":CPL1();
                    break;
                case "01110":CPE1();
                    break;
                case "01111":BR();
                    break;
                case "10000":BRT();
                    break;
                case "10001":
                    BRF();
                    break;
                case "10010":CALL();
                    break;
                case "10011":break;
                case "10100":break;
                case "10101":break;
                case "10110":
                    PUSH();
                    break;
                case "10111":String temp=pop();
                    return temp;
                case "11000":break;
        }

    }
    return "";
    }

    //0 address instructions, as per the instruction set, which are called in execute()

    public static void OR(){
        int temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])|conversions.bintodec(Loader.p.Stack[Loader.p.TOS-1]);
        Loader.p.Stack[Loader.p.TOS-1]=conversions.dectobin(temp);
        Loader.p.TOS--;
    }

    public static void AND(){
        int temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])&conversions.bintodec(Loader.p.Stack[Loader.p.TOS-1]);
        Loader.p.Stack[Loader.p.TOS-1]=conversions.dectobin(temp);
        Loader.p.TOS--;
    }

    public static void NOT(){
        int temp = ~conversions.bintodec(Loader.p.Stack[Loader.p.TOS]);
        Loader.p.Stack[Loader.p.TOS]=conversions.dectobin(temp);
    }

    public static void XOR(){
        int temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])^conversions.bintodec(Loader.p.Stack[Loader.p.TOS-1]);
        Loader.p.Stack[Loader.p.TOS-1]=conversions.dectobin(temp);
        Loader.p.TOS--;
    }

    public static void ADD(){
        int temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])+conversions.bintodec(Loader.p.Stack[Loader.p.TOS-1]);
        Loader.p.Stack[Loader.p.TOS-1]=conversions.dectobin(temp);
        Loader.p.TOS--;
    }

    public static void SUB(){
        int temp =conversions.bintodec(Loader.p.Stack[Loader.p.TOS-1])-conversions.bintodec(Loader.p.Stack[Loader.p.TOS]);
        Loader.p.Stack[Loader.p.TOS-1]=conversions.dectobin(temp);
        Loader.p.TOS--;
    }

    public static void MUL(){
        int temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])*conversions.bintodec(Loader.p.Stack[Loader.p.TOS-1]);
        Loader.p.Stack[Loader.p.TOS-1]=conversions.dectobin(temp);
        Loader.p.TOS--;
    }

    public static void DIV(){
        int temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])/conversions.bintodec(Loader.p.Stack[Loader.p.TOS-1]);
        Loader.p.Stack[Loader.p.TOS-1]=conversions.dectobin(temp);
        Loader.p.TOS--;
    }

    public static void MOD(){
        int temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])%conversions.bintodec(Loader.p.Stack[Loader.p.TOS-1]);
        Loader.p.Stack[Loader.p.TOS-1]=conversions.dectobin(temp);
        Loader.p.TOS--;
    }

    public static void SL(){
        int temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])<<1;
        Loader.p.Stack[Loader.p.TOS]=conversions.dectobin(temp);
    }

    public static void SR(){
        int temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])>>1;
        Loader.p.Stack[Loader.p.TOS]=conversions.dectobin(temp);
    }

    public static void CPG(){
        boolean temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])<conversions.bintodec(Loader.p.Stack[Loader.p.TOS-1]);
        if(temp==true) {
            Loader.p.Stack[Loader.p.TOS + 1] = "1111111111111111";
        } else{
            Loader.p.Stack[Loader.p.TOS + 1] = "0000000000000000";
        }
        Loader.p.TOS++;
    }

    public static void CPL(){
        boolean temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])>conversions.bintodec(Loader.p.Stack[Loader.p.TOS-1]);

        if(temp==true) {
            Loader.p.Stack[Loader.p.TOS + 1] = "1111111111111111";
        } else{
            Loader.p.Stack[Loader.p.TOS + 1] = "0000000000000000";
        }
        Loader.p.TOS++;
    }

    public static void CPE(){
        boolean temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])==conversions.bintodec(Loader.p.Stack[Loader.p.TOS-1]);
        if(temp==true) {
            Loader.p.Stack[Loader.p.TOS + 1] = "1111111111111111";
        } else{
            Loader.p.Stack[Loader.p.TOS + 1] = "0000000000000000";
        }
        Loader.p.TOS++;
    }

    public static String RD(){
        Loader.p.clock+=5;
        Loader.p.segfaults+=5;
        Loader.p.memclock+=5;
        String temp=Memory.memory("read",Integer.toString(Loader.p.ip_start+Loader.p.offset),null);
        int ip=conversions.bintodec(temp);
        Loader.p.ids+=conversions.dectobin(ip)+"\n";
        Loader.p.Stack[Loader.p.TOS+1]=conversions.dectobin(ip);
        Loader.p.TOS++;
        Loader.p.offset++;
        if(ip>8192){
            ERROR_HANDLER.inputrange();
        }
        return "";
    }

    public static String WR(){
        if(!Loader.p.flag0){
            Loader.p.clock+=5;
            Loader.p.memclock+=5;
            Loader.p.segfaults+=5;
            Loader.p.write=Loader.p.output_start;
        }
        String temp=Memory.memory("write",Integer.toString(Loader.p.write),Loader.p.Stack[Loader.p.TOS]);
        Loader.p.output=Loader.p.output+Loader.p.Stack[Loader.p.TOS]+"\n";
        Loader.p.write++;
        Loader.p.TOS--;
        Loader.p.flag0=true;
        return "";
    }

    public static void RET(){
        Loader.p.PC=conversions.bintodec(Loader.p.Stack[Loader.p.TOS]);
        Loader.p.TOS--;
    }

    //1 address instructions, as per the instruction set, which are called in execute()

    public static void OR1(){
        int temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])|conversions.bintodec(Loader.p.EAins);
        Loader.p.Stack[Loader.p.TOS]=conversions.dectobin(temp);
    }

    public static void AND1(){
        int temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])&conversions.bintodec(Loader.p.EAins);
        Loader.p.Stack[Loader.p.TOS]=conversions.dectobin(temp);
    }


    public static void XOR1(){
        int temp=conversions.bintodec(Loader.p.Stack[Loader.p.TOS])^conversions.bintodec(Loader.p.EAins);
        Loader.p.Stack[Loader.p.TOS]=conversions.dectobin(temp);
    }

    public static void ADD1(){
        int temp=conversions.bintodec(Loader.p.Stack[Loader.p.TOS])+conversions.bintodec(Loader.p.EAins);
        Loader.p. Stack[Loader.p.TOS]=conversions.dectobin(temp);
    }

    public static void SUB1(){
        int temp=conversions.bintodec(Loader.p.Stack[Loader.p.TOS])-conversions.bintodec(Loader.p.EAins);
        Loader.p.Stack[Loader.p.TOS]=conversions.dectobin(temp);
    }

    public static void MUL1(){
        int temp=conversions.bintodec(Loader.p.Stack[Loader.p.TOS])*conversions.bintodec(Loader.p.EAins);
        Loader.p.Stack[Loader.p.TOS]=conversions.dectobin(temp);
    }

    public static void DIV1(){
        int temp=conversions.bintodec(Loader.p.Stack[Loader.p.TOS])/conversions.bintodec(Loader.p.EAins);
        Loader.p.Stack[Loader.p.TOS]=conversions.dectobin(temp);
    }

    public static void MOD1(){
        int temp=conversions.bintodec(Loader.p.Stack[Loader.p.TOS])%conversions.bintodec(Loader.p.EAins);
        Loader.p.Stack[Loader.p.TOS]=conversions.dectobin(temp);
    }

    public static void CPG1(){
        int result=conversions.bintodec(Loader.p.EAins);
        boolean temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])>result;
        if(temp) {
            Loader.p.Stack[Loader.p.TOS + 1] = "1111111111111111";
        } else{
            Loader.p.Stack[Loader.p.TOS + 1] = "0000000000000000";
        }
        Loader.p.TOS++;
    }

    public static void CPL1(){
        int result=conversions.bintodec(Loader.p.EAins);
        boolean temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])<result;
        if(temp) {
            Loader.p.Stack[Loader.p.TOS + 1] = "1111111111111111";
        } else{
            Loader.p.Stack[Loader.p.TOS + 1] = "0000000000000000";
        }
        Loader.p.TOS++;
    }

    public static void CPE1(){
        boolean temp = conversions.bintodec(Loader.p.Stack[Loader.p.TOS])==conversions.bintodec(Loader.p.EAins);
        if(temp) {
            Loader.p.Stack[Loader.p.TOS + 1] = "1111111111111111";
        } else{
            Loader.p.Stack[Loader.p.TOS + 1] = "0000000000000000";
        }
        Loader.p.TOS++;
    }

    public static void BR(){
        Loader.p.PC=Loader.p.EA;
    }

    public static void BRT(){
        if(Loader.p.Stack[Loader.p.TOS].equals("1111111111111111")){
            Loader.p.PC=Loader.p.EA;
        }
        Loader.p.TOS--;
    }

    public static void BRF(){
        if(Loader.p.Stack[Loader.p.TOS].equals("0000000000000000")){
            Loader.p.PC=Loader.p.EA;
        }
        Loader.p.TOS--;
    }

    public static void CALL(){
        Loader.p.TOS++;
        Loader.p.Stack[Loader.p.TOS]=conversions.dectobin(Loader.p.PC);
        Loader.p.PC=Loader.p.EA;
    }

    public static void PUSH(){
        Loader.p.TOS++;
        Loader.p.Stack[Loader.p.TOS]=Loader.p.EAins;
    }

    public static String pop(){
        String temp=Memory.memory("write",Integer.toString(Loader.p.EA),Loader.p.Stack[Loader.p.TOS]);
        DISK.write(Loader.p.EA,Loader.p.Stack[Loader.p.TOS]);
        Loader.p.TOS--;
        return "";
    }
}
