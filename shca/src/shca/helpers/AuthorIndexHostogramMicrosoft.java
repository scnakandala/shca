package shca.helpers;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Supun Nakandala
 */
public class AuthorIndexHostogramMicrosoft {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        String temp = "50_Thomas A. Henzinger";
        String inFileName = "./authors/" + temp + "/publications_microsoft.txt";
        String outFileName = "./authors/" + temp + "/author_index_histo_microsoft.txt";
        String authorName = "henzinger";

        File file = new File(inFileName);
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        
        BufferedReader reader = new BufferedReader(new FileReader(inFileName));
        String input = reader.readLine();
        int index = 0;        
        int[] authorIndexesHisto = new int[10];
        while(input!=null){
            if(input.startsWith("A1")){
                String author = input.split(" ")[1].toLowerCase();
                if(author.contains(authorName) && index < 10){
                    authorIndexesHisto[index] += 1;
                }
                index++;
            }
            if(input.isEmpty()){
                index=0;
            }
            
            input = reader.readLine();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));
        for (int i = 0; i < 10; i++) {
            writer.write(authorIndexesHisto[i] + " ");
        }
        writer.flush();
        writer.close();
    }
}
