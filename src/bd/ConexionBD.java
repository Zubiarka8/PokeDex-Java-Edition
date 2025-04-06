package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD
{

	// Método para obtener la conexión con la base de datos
	public static Connection obtenerConexion() throws SQLException
	{
		// URL de conexión, usuario y contraseña
		String	url			= "jdbc:mysql://localhost:3306/PokemonAPP?allowPublicKeyRetrieval=true&useSSL=false";
		String	user		= "root";																				// Cambia
																													// con
																													// tu
																													// usuario
		String	password	= "root";																				// Cambia
																													// con
																													// tu
																													// contraseña

		try
		{
			// Cargar el driver JDBC
			Class.forName("com.mysql.cj.jdbc.Driver");
		}
		catch (ClassNotFoundException e)
		{
			System.err.println("Error al cargar el controlador: " + e.getMessage());
			throw new SQLException("Driver no encontrado", e);
		}

		// Establecer la conexión
		return DriverManager.getConnection(url, user, password);
	}

}
