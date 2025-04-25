package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MysqlSesioKudeaketa implements SesioKudeaketa
{

    /**
     * Datu baserako konexioa
     */
    public static Connection getKonexioa() throws SQLException
    {
        // datu baserako lotura, super erabiltzailea eta pasahitza
        String  url         = "jdbc:mysql://localhost:3306/PokemonAPP";
        String  user        = "root";
        String  password    = "root";

        // Konexioa ezarri
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Sesioa irekitzeko erabiltzaile batekin
     * 
     * @param erabiltzaileIzena (String)
     * @param pasahitza         (String)
     * @return Sesioa irekitzeko komprobaketa (boolean)
     */
    public boolean irekiSesioa(String erabiltzaileIzena, String pasahitza)
    {
        // Hasierako autentifikazioa
        boolean autentifikazioa = false;

        String  kontsulta       = "SELECT ErabiltzaileIzena, pasahitza FROM Erabiltzaileak WHERE ErabiltzaileIzena = ? and pasahitza  = ?";

        try (Connection conn = getKonexioa(); PreparedStatement pstmt = conn.prepareStatement(kontsulta))
        {

            pstmt.setString(1, erabiltzaileIzena);
            pstmt.setString(2, pasahitza);

            ResultSet rs = pstmt.executeQuery();

            if(rs.next())
            {
                autentifikazioa = true;
            }

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return autentifikazioa;
    }

}
