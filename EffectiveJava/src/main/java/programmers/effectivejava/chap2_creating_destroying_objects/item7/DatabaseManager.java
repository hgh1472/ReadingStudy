package programmers.effectivejava.chap2_creating_destroying_objects.item7;

import programmers.effectivejava.chap2_creating_destroying_objects.item7.vo.ResultSet;

public interface DatabaseManager {
    ResultSet execute(final String query);
}
