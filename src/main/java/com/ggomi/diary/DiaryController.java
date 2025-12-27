package com.ggomi.diary;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.*;

import javax.sql.DataSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 구성에 대한 내용
 * !!Fat Controller : 컨트롤러에 모든 기능을 다 적는것. 근데 큰 프로젝트에서 사용하기에는 너무 복잡해짐. 그래서 단순한 프로젝트나 단발성 테스트를 위해서 사용됨(지금 구현한것)
 * MVC : View(html, js, css, jsp, react) <-Contoller-> Model(실제 서버에서 동작할 기능들을 구현한 곳, Service, DTO, Mybatis, xml, Mapper)
 * 
 * DB 접근 내용
 * Mybatis, JPA, 순수 JDBC(지금 쓴것)
 * !!순수 JDBC(지금 쓴것) -> JdbcTemplate(스프링에서 지원해주는 라이브러리) -> Mybatis -> JPA
 */
@RestController
public class DiaryController {

    private final DataSource dataSource;

    // 스프링 컨테이너에 보관 중인 DataSource를 주입받습니다.
    // 생성자
    public DiaryController(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /*
        fetch('http://localhost:8080/diary', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: '테스트 사용자', entry_date: '2026-01-01' })
        }).then(res => res.text()).then(console.log);
     */
    // 신규 일기 추가
    @PostMapping("/diary")
    public String create(@RequestBody Map<String, String> param) {
        // 추가 쿼리 선인
        String sql = "INSERT INTO daily_diaries (content, entry_date) VALUES (?, ?)";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            // DB와 연결
            // 연결될 경우 close 메소드를 이용해서 연결을 끊어주지 않으면, localhost 서버가 종료되더라도 계속 연결 상태로 유지됨
            connection = dataSource.getConnection();

            // 연결된 DB에 쿼리를 보낸 뒤에 그 쿼리를 실행할 객체를 preparedStatement라는 변수에 담는다.
            // 연결될 경우 close 메소드를 이용해서 연결을 끊어주지 않으면, localhost 서버가 종료되더라도 계속 연결 상태로 유지됨
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, param.get("content"));

            // DATE 타입의 컬럼에 값을 저장하기 위해서는 setDate 메소드를 사용하여 Date 타입의 값을 세팅해서 보내줘야함
            String dateStr = param.get("entry_date");
            Date sqlDate = Date.valueOf(dateStr);
            preparedStatement.setDate(2, sqlDate);

            preparedStatement.executeUpdate();

            return "추가 성공";
        } catch (Exception e) {
            e.printStackTrace();
            return "추가 실패";
        } finally {
            // try 로직이 정상적으로 종료되든, Exception이 발생하여 catch로 빠지든 상관없이 항상 마지막으로 실행되는 부분이 finally 부분이다.
            // 그래서 이 곳에서 connection과 preparedStatement를 close해줘야, 정상적으로 진행되던 exception이 발생하던 상관없이 마지막에 연결을 끊어줄 수 있다.
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 일기 목록 조회
    /**
     * Map<String, Object> : Key의 타입이 String, Value의 타입이 Object라는 뜻
     */
    @GetMapping("/diary")
    public List<Map<String, Object>> getDiaryList() {
        // 목록 조회 결과를 담을 리스트 선언
        List<Map<String, Object>> dataList = new ArrayList<>();

        // 조회 쿼리 선언
        String sql = "SELECT id, content FROM daily_diaries";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            // DB와 연결
            // 연결될 경우 close 메소드를 이용해서 연결을 끊어주지 않으면, localhost 서버가 종료되더라도 계속 연결 상태로 유지됨
            connection = dataSource.getConnection();

            // 연결된 DB에 쿼리를 보낸 뒤에 그 쿼리를 실행할 객체를 preparedStatement라는 변수에 담는다.
            // 연결될 경우 close 메소드를 이용해서 연결을 끊어주지 않으면, localhost 서버가 종료되더라도 계속 연결 상태로 유지됨
            preparedStatement = connection.prepareStatement(sql);

            // 입력한 쿼리를 실행한 뒤에 결과값을 resultSet이라는 변수에 담는다.
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", resultSet.getLong("id"));
                data.put("content", resultSet.getString("content"));

                dataList.add(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // try 로직이 정상적으로 종료되든, Exception이 발생하여 catch로 빠지든 상관없이 항상 마지막으로 실행되는 부분이 finally 부분이다.
            // 그래서 이 곳에서 connection과 preparedStatement를 close해줘야, 정상적으로 진행되던 exception이 발생하던 상관없이 마지막에 연결을 끊어줄 수 있다.
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return dataList;
    }

    // 일기 수정
    // GET, POST, PUT, PATCH, DELETE, CONNECT, COPY, MOVE
    // RestController에서 지원하는 HTTP Method : GET, POST, PUT, PATCH, DELETE

    /**
     * 옛날 버전
     * GET : 조회
     * POST : 추가, 수정, 삭제
     */
    /**
     * 대충 버전
     * GET : 조회
     * POST : 추가
     * PUT : 수정
     * DELETE : 삭제
     */
    /**
     * 정석 버전
     * GET : 조회
     * POST : 추가
     * PUT : 전체 내용 수정
     * PATCH : 부분 내용 수정
     * DELETE : 삭제
     */

    
    /*
        fetch('http://localhost:8080/diary/1', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: '수정하는 방법 배움' })
        }).then(res => res.text()).then(console.log);
     */
    @PutMapping("/diary/{id}")
    public String update(@PathVariable("id") Long id, @RequestBody Map<String, String> param) {
        // 수정 쿼리 선인
        String sql = "UPDATE daily_diaries SET content = ?, updated_at = ? WHERE id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            // DB와 연결
            // 연결될 경우 close 메소드를 이용해서 연결을 끊어주지 않으면, localhost 서버가 종료되더라도 계속 연결 상태로 유지됨
            connection = dataSource.getConnection();

            // 연결된 DB에 쿼리를 보낸 뒤에 그 쿼리를 실행할 객체를 preparedStatement라는 변수에 담는다.
            // 연결될 경우 close 메소드를 이용해서 연결을 끊어주지 않으면, localhost 서버가 종료되더라도 계속 연결 상태로 유지됨
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, param.get("content"));

            // 현재 시간을 저장
            Date sqlDate = new Date(System.currentTimeMillis());
            preparedStatement.setDate(2, sqlDate);

            // 수정할 id 세팅
            preparedStatement.setLong(3, Long.valueOf(id));

            preparedStatement.executeUpdate();

            return "수정 성공";
        } catch (Exception e) {
            e.printStackTrace();
            return "수정 실패";
        } finally {
            // try 로직이 정상적으로 종료되든, Exception이 발생하여 catch로 빠지든 상관없이 항상 마지막으로 실행되는 부분이 finally 부분이다.
            // 그래서 이 곳에서 connection과 preparedStatement를 close해줘야, 정상적으로 진행되던 exception이 발생하던 상관없이 마지막에 연결을 끊어줄 수 있다.
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 일기 삭제

    
    /*
        fetch('http://localhost:8080/diary/11', {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' }
        }).then(res => res.text()).then(console.log);
     */
    @DeleteMapping("/diary/{id}")
    public String delete(@PathVariable("id") Long id) {
        // 수정 쿼리 선인
        String sql = "DELETE FROM daily_diaries WHERE id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            // DB와 연결
            // 연결될 경우 close 메소드를 이용해서 연결을 끊어주지 않으면, localhost 서버가 종료되더라도 계속 연결 상태로 유지됨
            connection = dataSource.getConnection();

            // 연결된 DB에 쿼리를 보낸 뒤에 그 쿼리를 실행할 객체를 preparedStatement라는 변수에 담는다.
            // 연결될 경우 close 메소드를 이용해서 연결을 끊어주지 않으면, localhost 서버가 종료되더라도 계속 연결 상태로 유지됨
            preparedStatement = connection.prepareStatement(sql);

            // 삭제할 id 세팅
            preparedStatement.setLong(1, Long.valueOf(id));

            preparedStatement.executeUpdate();

            return "삭제 성공";
        } catch (Exception e) {
            e.printStackTrace();
            return "삭제 실패";
        } finally {
            // try 로직이 정상적으로 종료되든, Exception이 발생하여 catch로 빠지든 상관없이 항상 마지막으로 실행되는 부분이 finally 부분이다.
            // 그래서 이 곳에서 connection과 preparedStatement를 close해줘야, 정상적으로 진행되던 exception이 발생하던 상관없이 마지막에 연결을 끊어줄 수 있다.
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
