package tk.twpooi.tuetue;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by tw on 2016-09-23.
 */
public class FileManager {

    private Context context;

    private static final String favoriteFileName = "favorite.tt";
    private static final String myCourseFileName = "mycourse.tt";
    private static final String sharedFileName = "Shared.tt";
    private static final String courseGameFileName = "coursegame.tt";
    private static final String interestTutorFileName = "interestTutor.tt";

    public FileManager(Context context){
        this.context = context;
    }


    public ArrayList<String> readInterestListFile(){

        ArrayList<String> list = new ArrayList<>();

        ObjectInputStream input;

        try{
            input = new ObjectInputStream(new FileInputStream(new File(new File(context.getFilesDir(), "") + File.separator + interestTutorFileName)));
            list = (ArrayList<String>)input.readObject();
            input.close();
        }catch(FileNotFoundException e){
        }catch(IOException e){
        }catch(Exception e){
        }

        return list;

    }

    public void addInterestList(String id){

        ArrayList<String> list = readInterestListFile();
        list.add(id);
        writeInterestListFile(list);

    }

    public void removeInterestList(String id){

        ArrayList<String> list = readInterestListFile();
        list.remove(id);
        writeInterestListFile(list);

    }

    public void writeInterestListFile(ArrayList<String> list){

        ObjectOutput out = null;

        try{
            out = new ObjectOutputStream(new FileOutputStream(new File(context.getFilesDir(),"")+ File.separator+interestTutorFileName));
            out.writeObject(list);
            out.close();
        }catch(FileNotFoundException e){
        }catch (IOException e){
        }

    }
}
