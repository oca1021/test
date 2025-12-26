package com.ggomi.diary;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.*;

import javax.sql.DataSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class DiaryController {

    private final DataSource dataSource;

    // 스프링 컨테이너에 보관 중인 DataSource를 주입받습니다.
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
}
