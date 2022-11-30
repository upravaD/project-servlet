package com.tictactoe;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Получаем текущую сессию.
        HttpSession currentSession = req.getSession();

        // Получаем объект игрового поля сессии.
        Field field = extractField(currentSession);

        // Получаем индекс ячейки, по которой произошел клик.
        int index = getSelectedIndex(req);
        Sign currentSign = field.getField().get(index);

        // Проверяем, что ячейка, по которой был клик пустая.
        // Иначе ничего не делаем и отправляем пользователя на ту же страницу без изменений параметров в сессии.
        if (Sign.EMPTY != currentSign) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, resp);
            return;
        }

        // Ставим крестик в ячейке, по которой кликнул пользователь.
        field.getField().put(index, Sign.CROSS);

        // Проверяем, не победил ли крестик после добавления последнего клика пользователя.
        if (checkWin(resp, currentSession, field)) {
            return;
        }

        // Получаем пустую ячейку поля.
        int emptyFieldIndex = field.getEmptyFieldIndex();

        // Если пустая ячейка присутствует.
        if (emptyFieldIndex >= 0) {
            // Ставим нолик.
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
        // Если пустой ячейки нет и никто не победил - значит это ничья.
        } else {
            // Добавляем в сессию флаг, который сигнализирует что произошла ничья.
            currentSession.setAttribute("draw", true);
            // Считаем список значков.
            List<Sign> data = field.getFieldData();
            // Обновляем этот список в сессии.
            currentSession.setAttribute("data", data);
            // Шлём редирект.
            resp.sendRedirect("/index.jsp");
            return;
        }

        // Проверяем, не победил ли нолик после добавление последнего нолика.
        if (checkWin(resp, currentSession, field)) {
            return;
        }

        // Считаем список значков.
        List<Sign> data = field.getFieldData();

        // Обновляем объект поля и список значков в сессии.
        currentSession.setAttribute("data", data);
        currentSession.setAttribute("field", field);

        // Шлём редирект.
        resp.sendRedirect("/index.jsp");
    }

    private int getSelectedIndex(HttpServletRequest request) {
        String click = request.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    private Field extractField(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }
        return (Field) fieldAttribute;
    }

    /**
     * Метод проверяет, нет ли трех крестиков/ноликов в ряд.
     * Возвращает true/false
     */
    private boolean checkWin(HttpServletResponse response, HttpSession currentSession, Field field) throws IOException {
        Sign winner = field.checkWin();
        if (Sign.CROSS == winner || Sign.NOUGHT == winner) {
            // Добавляем флаг, который показывает что кто-то победил.
            currentSession.setAttribute("winner", winner);

            // Считаем список значков.
            List<Sign> data = field.getFieldData();

            // Обновляем этот список в сессии.
            currentSession.setAttribute("data", data);

            // Шлём редирект.
            response.sendRedirect("/index.jsp");
            return true;
        }
        return false;
    }
}
