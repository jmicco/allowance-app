package org.jmicco.parentbank.parentdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class TestDatabaseHelper {
	Logger logger = Logger.getLogger(TestDatabaseHelper.class.getName());
	private final EntityManagerFactory emf;
	private final EntityManager em;
	
	public TestDatabaseHelper() throws IOException, SQLException {
		emf = Persistence.createEntityManagerFactory("testing");
		em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		
		File file = new File(em.getClass().getClassLoader().getResource("").getPath());
		file = new File(file, "../../../Database/server_parentdb.sql");
		System.out.println("PATH: " + file.getPath());
		InputStream inputSql = new FileInputStream(file);
		String sql = convertStringToString(inputSql);
		
		tx.begin();
		Connection connection = em.unwrap(Connection.class);
		String [] statements = sql.split(";");
		for (String statement : statements) {
			PreparedStatement preparedStatement = connection.prepareStatement(statement);
			logger.info("executing: " + statement);
			preparedStatement.execute();
		}
		
		PreparedStatement preparedStatement = connection.prepareStatement("show tables from parentdb");
		boolean resultsAvailable = preparedStatement.execute();
		ResultSet resultSet = preparedStatement.getResultSet();
		resultSet.first();
		while (!resultSet.isAfterLast()) {
			logger.info("Table: " + resultSet.getString(1) + " schema: " + resultSet.getString(2));
			resultSet.next();
		}
		tx.commit();
	}

	private String convertStringToString(InputStream inputSql) throws IOException {
		Scanner scanner = new Scanner(inputSql);
		scanner.useDelimiter("\\A:");
		String result = scanner.hasNext() ? scanner.next() : "";
		scanner.close();
		inputSql.close();
		return result;
	}
	
	public void close() {
		em.close();
		emf.close();
	}

	public EntityManager getEm() {
		return em;
	}
}
