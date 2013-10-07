package shca;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 *
 * @author Supun Nakandala
 */
public class CiteSeer {

    public static void main(String[] args) throws FileNotFoundException, IOException, DocumentException, SQLException, ClassNotFoundException {
        insertData();
    }

    public static void insertData() throws SQLException, ClassNotFoundException, FileNotFoundException, IOException, DocumentException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager
                .getConnection("jdbc:mysql://localhost/shca", "root", "");

        connection.setAutoCommit(false);

        File file = new File("./oai_citeseer/oai_citeseer20.dump");
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();

        String text = new String(data, "UTF8");
        Document document = (Document) DocumentHelper.parseText(text);

        Element root = document.getRootElement();
        for (Iterator i = root.elementIterator(); i.hasNext();) {
            Element record = (Element) i.next();

            Element header = record.element("header");
            Element tempElement;

            tempElement = header.element("identifier");
            String stringPaperId;
            if (tempElement != null) {
                stringPaperId = tempElement.getText().split(":")[2];
            } else {
                continue;
            }

            Element metadata = record.element("metadata");

            tempElement = metadata.element("oai_citeseer");
            String title = "";
            if (tempElement.element("title") != null) {
                title = tempElement.element("title").getText();
            } else {
                continue;
            }

            List<Element> authors = tempElement.elements("author");
            String[] authorNames;
            if (authors != null && authors.size() > 0) {
                authorNames = new String[authors.size()];
                for (int l = 0; l < authorNames.length; l++) {
                    authorNames[l] = authors.get(l).attribute("name").getText().replaceAll("'", "");
                }
            } else {
                continue;
            }

            String paperAbstract = "";
            if (tempElement.element("description") != null) {
                paperAbstract = tempElement.element("description").getText();
            }


            String pubYear = "unknown";
            if (tempElement.element("pubyear") != null) {
                pubYear = tempElement.element("pubyear").getText();
            }

            String publishedAtOrIn = "unknown";
            if (tempElement.element("publisher").getText() != null) {
                publishedAtOrIn = tempElement.element("publisher").getText();
            }

            List<Element> relations = tempElement.elements("relation");
            String[] citations = null;
            if (relations != null && relations.size() > 0) {
                citations = new String[relations.size()];
                for (int l = 0; l < citations.length; l++) {
                    citations[l] = relations.get(l).element("uri").getText().split(":")[2];
                }
            }
            try {
                Statement statement = connection.createStatement();
                PreparedStatement preparedStatement;
                String query;

                String[] authorsIds = new String[authorNames.length];
                for (int l = 0; l < authorNames.length; l++) {
                    query = "select id from authors where name='" + authorNames[l] + "';";
                    ResultSet results = statement.executeQuery(query);
                    if (results.next()) {
                        authorsIds[l] = results.getString("id");
                        connection.rollback();
                        continue;
                    }
                    query = "insert into authors(name) value('" + authorNames[l] + "');";
                    statement.executeUpdate(query);
                    query = "select id from authors where name='" + authorNames[l] + "';";
                    results = statement.executeQuery(query);
                    if (results.next()) {
                        authorsIds[l] = results.getString("id");
                    }
                }

                preparedStatement = connection.prepareStatement(
                        "insert into papers(id,title,published_year,published_at_in,abstract)"
                        + " values(?,?,?,?,?)");
                preparedStatement.setString(1, stringPaperId);
                preparedStatement.setString(2, title);
                preparedStatement.setString(3, pubYear);
                preparedStatement.setString(4, publishedAtOrIn);
                preparedStatement.setString(5, paperAbstract);
                preparedStatement.executeUpdate();

                for (int l = 0; l < authorsIds.length; l++) {
                    preparedStatement = connection.prepareStatement(
                            "insert into author_paper(author_id,paper_id,author_number)"
                            + " values(?,?,?)");
                    preparedStatement.setString(1, authorsIds[l]);
                    preparedStatement.setString(2, stringPaperId);
                    preparedStatement.setString(3, (l + 1) + "");
                    preparedStatement.executeUpdate();
                }

                if (citations != null) {
                    for (int l = 0; l < citations.length; l++) {
                        preparedStatement = connection.prepareStatement(
                                "insert into citations(citing_paper_id, cited_paper_id)"
                                + " values(?,?)");
                        preparedStatement.setString(1, stringPaperId);
                        preparedStatement.setString(2, citations[l]);
                        preparedStatement.executeUpdate();
                    }
                }
                connection.commit();
            } catch (Exception ex) {
                System.out.println(ex.toString());
                if (connection != null) {
                    connection.rollback();
                }
            }
        }
    }
}
