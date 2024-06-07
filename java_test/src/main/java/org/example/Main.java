package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

class Ticket {
    String origin;
    String origin_name;
    String destination;
    String destination_name;
    String departure_date;
    String departure_time;
    String arrival_date;
    String arrival_time;
    String carrier;
    int stops;
    int price;

    public long getFlightDuration() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy H:mm");
        LocalDateTime departureDateTime = LocalDateTime.parse(departure_date + " " + formatTime(departure_time), formatter);
        LocalDateTime arrivalDateTime = LocalDateTime.parse(arrival_date + " " + formatTime(arrival_time), formatter);
        return Duration.between(departureDateTime, arrivalDateTime).toMinutes();
    }

    private String formatTime(String time) {
        String[] parts = time.split(":");
        if (parts[0].length() == 1) {
            parts[0] = "0" + parts[0];
        }
        return parts[0] + ":" + parts[1];
    }
}

class Tickets {
    List<Ticket> tickets;
}

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Main <path_to_json_file>");
            return;
        }

        String filePath = args[0];
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(filePath)) {
            Type ticketListType = new TypeToken<Tickets>() {}.getType();
            Tickets ticketsData = gson.fromJson(reader, ticketListType);
            List<Ticket> tickets = ticketsData.tickets;

            List<Ticket> vvoToTlvTickets = tickets.stream()
                    .filter(ticket -> ticket.origin.equals("VVO") && ticket.destination.equals("TLV"))
                    .collect(Collectors.toList());

            Map<String, Long> minFlightTimes = new HashMap<>();
            for (Ticket ticket : vvoToTlvTickets) {
                long flightDuration = ticket.getFlightDuration();
                minFlightTimes.merge(ticket.carrier, flightDuration, Math::min);
            }

            System.out.println("Минимальное время полета между городами Владивостоком и Тель-Авив для каждого авиаперевозчика:");
            for (Map.Entry<String, Long> entry : minFlightTimes.entrySet()) {
                System.out.println("Авиаперевозчик: " + entry.getKey() + ", Минимальное время полета: " + entry.getValue() + " минут");
            }

            List<Integer> prices = vvoToTlvTickets.stream().map(ticket -> ticket.price).sorted().collect(Collectors.toList());

            double averagePrice = prices.stream().mapToInt(Integer::intValue).average().orElse(0);
            double medianPrice;
            int size = prices.size();
            if (size % 2 == 0) {
                medianPrice = (prices.get(size / 2 - 1) + prices.get(size / 2)) / 2.0;
            } else {
                medianPrice = prices.get(size / 2);
            }

            double priceDifference = averagePrice - medianPrice;
            System.out.println("Разница между средней ценой и медианой: " + priceDifference);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


