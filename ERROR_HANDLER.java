/*
This is the error handler. All the known normal exceptions are handled here and the appended to the output file

This module handles only a few of the many exceptions that may arise during the runtime of the system.
All the phase II specific errors are also handled in this class
 */

public class ERROR_HANDLER extends Exception{
    static void fileerror() {
        Loader.p.errors+="\tProblem while reading the file";
        exit();
    }

    static void improperinput(){
        Loader.p.errors+="\tInput is of not proper form";
        exit();
    }

    static void infinite(){
        Loader.p.errors+="\tInfinite job error";
        exit();
    }

    static void memoryflow(){
        Loader.p.errors+="\tMemory is not of proper size. Overflow error";
        exit();
    }

    static void stackerror(){
        Loader.p.errors+="\tStack underflow error";
        exit();
    }

    static void format(){
        Loader.p.errors+="\tGiven instruction/input is not a number";
        exit();
    }

    static void stackover(){
        Loader.p.errors+="\tStack overflow error";
        exit();
    }

    static void joberror(){
        Loader.p.errors+="\tMissing **JOB error";
        exit();
    }

    static void ldformaterror(){
        Loader.p.errors+="\tMissing/improper Loader format error";
        exit();
    }

    static void missingiperror(){
        Loader.p.errors+="\tMissing **INPUT error";
        exit();
    }

    static void iperror(){
        Loader.p.errors+="\tMore than 1 **INPUT error";
        exit();
    }

    static void inputrange(){
        Loader.p.errors+="\tInput is above acceptable range.";
        exit();
    }

    static void PCerror(){
        Loader.p.errors+="\tAddress out of range error.";
        exit();
    }

    static void arrayoutofindexsys(){
        Loader.p.errors+="\tloader header is improper";
        exit();
    }

    static void missinffin(){
        Loader.p.errors+="\tMissing **FIN error";
        exit();
    }

    static void dividezero(){
        Loader.p.errors+="\tDivide by zero error";
        exit();
    }

    static void address(){
        Loader.p.errors+="\tAddress out of range error";
        exit();
    }

    static void blankline(){
        Loader.p.errors+="\tBlank line is present in job";
        exit();
    }

    static void wrongipsize(){
        Loader.p.errors+="\tConflict between # of input words and input items given";
        exit();
    }

    static void abnormal(String error){
        Loader.p.errors+="\tDescriptive message: "+error;
        Loader.p.flag=true;
        exit();
    }

    //prints the output file and exits the system in the result of an error

    static String exit(){
        CPU.outputfile();
        return "END";
    }

}
