package programmers.effectivejava.chap2_creating_destroying_objects.item7;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Stack;

import programmers.effectivejava.chap2_creating_destroying_objects.item7.vo.DBConnection;

public class CallbackTest {

	@DisplayName("콜백으로 정의된 인터페이스를 필드에 직접 가지고 있으면 메모리누수")
	@Test
	public void memoryLeak() throws InterruptedException {
		Connection connection = createConnection("Memory Leak Connection");
		DatabaseManager databaseManager = new CustomDatabaseManager(connection);
		databaseManager.execute("SELECT * FROM TEST");

		connection = null; // 참조해제 시도

		System.gc();
		System.runFinalization();

		Thread.sleep(3000);

		assertNotNull(((CustomDatabaseManager) databaseManager).getConnection());
	}

	@DisplayName("필드에 참조된 콜백 인터페이스를 참조해제 하는 방법")
	@Test
	public void callbackMemory() {
		Connection connection = createConnection("Memory Leak Connection");
		DatabaseManager databaseManager = new CustomDatabaseManager(connection);

		((CustomDatabaseManager) databaseManager).deconnect();

		assertNull(((CustomDatabaseManager) databaseManager).getConnection());
	}

	@DisplayName("WeakHashMap 을 사용해서 메모리 누수 방지하는 방법")
	@Test
	public void weakHashMap() throws InterruptedException {
		Connection connection = createConnection("using WeakHashMap");
		DatabaseManager databaseManager = new CacheDatabaseManager(connection);

		connection = null; // 참조해제 시도

		System.gc();
		System.runFinalization();

		Thread.sleep(3000);

		assertThrows(RuntimeException.class, ((CacheDatabaseManager) databaseManager)::getConnection);
	}

	private Connection createConnection(String message) {
		return () -> {
			System.out.println(message);
			return new DBConnection();
		};
	}
}
