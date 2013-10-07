package shca;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 *
 * @author Supun Nakandala
 */
public class AuthorIndexHistogramGoogle {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, DocumentException {
        String temp = "6_Jiawei Han";
        String inFileName = "./authors/" + temp + "/publications_google.txt";
        String outFileName = "./authors/" + temp + "/author_index_histo_google.txt";
        String authorName = "han";

        File file = new File(inFileName);
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();

        String text = new String(data, "UTF8");
        Document document = (Document) DocumentHelper.parseText(text);

        int[] authorIndexesHisto = new int[10];
        Element root = document.getRootElement();
        for (Iterator i = root.elementIterator(); i.hasNext();) {
            Element record = (Element) i.next();
            String stringAuthors = record.element("authors").getText();
            String[] authors = stringAuthors.split(",");

            for (int j = 0; j < Math.min(10, authors.length); j++) {
                if (authors[j].toLowerCase().contains(authorName)) {
                    authorIndexesHisto[j] += 1;
                }
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));
        for (int i = 0; i < 10; i++) {
            writer.write(authorIndexesHisto[i] + " ");
        }
        writer.flush();
        writer.close();
    }
}
