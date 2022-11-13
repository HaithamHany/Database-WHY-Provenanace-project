import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.checkerframework.checker.units.qual.A;
import org.jooq.lambda.Seq;

import java.sql.*;
import java.util.*;
import java.util.Collections;
import java.util.stream.Collectors;

public class Main {

    public static void main(String args[]) throws SQLException {

        //Establishing database connection using the database owner credentials
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/adbTest",
                            "Haitham", "123456789");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");

        //After connecting successfully we execute PostgreSql query to get the annotation coloumns
        //from the respective tables

        ArrayList<String> R1 = GetAnnotationColumn(c,"select ann from public.\"region\" ;", "ann" );
        ArrayList<String> R2 = GetAnnotationColumn(c,"select  ann from public.\"nation\" ;", "ann" );

        GetWhyProvenance(R1, R2, c);
    }

    static void GetWhyProvenance (List<String> R1, List<String> R2, Connection c) throws SQLException {

        //Converting the Rows ot R1 and R2 to immutable sets
        Set<String> setA = ImmutableSet.copyOf(R1);
        Set<String> setB = ImmutableSet.copyOf(R2);

        //Getting the Cross product using Guava
        Set<List<String>> product = Sets.cartesianProduct(setA,setB);

        //Filtering the Cross product result to get the actual results based on the condition
        List<Tuple> resultTuples = Filter(product, c);

        //Sort the Tuples
        List<Tuple> sortedTuples = sortedResult(resultTuples);

        //Aggregate the results
        AggregateResult(sortedTuples);
        c.close();
    }

    static List<Tuple> Filter(Set<List<String>> product, Connection c) throws SQLException {

        List<Tuple> resultTuples = new ArrayList<>();

        for (List<String> s : product) {

            int r_region_region = 0;
            int n_region_nation = 0;

            String regionAnnotation = s.get(0);
            String nationAnnotation = s.get(1);
            String regionQuery = String.format("select r_regionkey from public.\"region\" where ann = '%s';", regionAnnotation);
            String nationQuery = String.format("select n_regionkey from public.\"nation\" where ann = '%s';", nationAnnotation);

            r_region_region = GetValueOfAttribute(c, regionQuery, "r_regionkey");
            n_region_nation = GetValueOfAttribute(c, nationQuery, "n_regionkey");


            if(r_region_region == n_region_nation)
            {
                Tuple t = new Tuple(s.get(0), s.get(1), r_region_region);
                resultTuples.add(t);
            }
        }

        return resultTuples;
    }

    static List<Tuple> sortedResult(List<Tuple> resultTuples)
    {
        Comparator<Tuple> compareByValue = Comparator.comparing(Tuple::getValueId);

        resultTuples.sort(compareByValue);
        return resultTuples;
    }

    static void AggregateResult(List<Tuple> resultTuples)
    {

        Map<Object, List<Tuple>> finalResult =
                resultTuples.stream().collect(Collectors.groupingBy(w -> w.valueId));

        Map<Object ,List<ResultPair>> resultPairList = new HashMap<>();

        for(List<Tuple> entry : finalResult.values())
        {
            List<ResultPair> rpl = new ArrayList<ResultPair>();
            for(Tuple item : entry)
            {
                ResultPair rp = new ResultPair(item.ann1, item.ann2);
                rpl.add(rp);
                resultPairList.put(item.valueId, rpl);
            }
        }

        for (Object entry : resultPairList.entrySet())
            System.out.println(entry);
    }

    //Getting values of attribute
    static private int GetValueOfAttribute(Connection c, String query, String coloumn) throws SQLException {
        int result = 0;
        Statement stmt1 = c.createStatement();
        ResultSet regionResult = stmt1.executeQuery(query);

        if (regionResult.next())
        {
            result = regionResult.getInt(coloumn);
        }

        return result;
    }

    static private ArrayList<String> GetAnnotationColumn (Connection c, String query, String coloumn) throws SQLException {

        Statement stmt = c.createStatement();
        ArrayList<String> R1 = new ArrayList<String>();

        ResultSet r1 = stmt.executeQuery(query);
        while (r1.next())
        {
            String annotation = r1.getString(coloumn);
            R1.add(annotation);
        }

        r1.close();
        stmt.close();

        return R1;
    }
}

