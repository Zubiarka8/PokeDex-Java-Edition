package interfaz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import bd.ConexionBD;

public class LoginVentana
{

	// Método para autenticar a un usuario
	public boolean autenticarUsuario(String nombreUsuario, String contrasena)
	{
		boolean	autenticado	= false;

		// Definir la consulta SQL
		String	query		= "SELECT * FROM login WHERE nombre_de_usuario = ? AND contraseña = ?";

		try (Connection connection = ConexionBD.obtenerConexion();
				PreparedStatement ps = connection.prepareStatement(query))
		{

			// Establecer los parámetros de la consulta
			ps.setString(1, nombreUsuario);
			ps.setString(2, contrasena); // Aquí deberías aplicar un proceso de hashing a la contraseña

			// Ejecutar la consulta
			ResultSet rs = ps.executeQuery();

			// Si existe un resultado, el usuario está autenticado
			if(rs.next())
			{
				autenticado = true;
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return autenticado;
	}

}
