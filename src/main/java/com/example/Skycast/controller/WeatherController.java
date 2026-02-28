package com.example.Skycast.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;

@Controller
public class WeatherController {

    @Value("${weather.api.key}")
    private String apiKey;

    @GetMapping("/")
    public String redirect(){
        return "index";
    }

    @GetMapping("/weather")
    public String getWeather(@RequestParam("city") String city, Model model) {

        try {

            String url = "https://api.openweathermap.org/data/2.5/weather?q="
                    + city
                    + "&units=metric"
                    + "&appid="+apiKey;

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            System.out.println(response);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            double temp = root.path("main").path("temp").asDouble();
            int humidity = root.path("main").path("humidity").asInt();
            String description = root.path("weather").get(0).path("description").asText();
            double windSpeedMs = root.path("wind").path("speed").asDouble();
            double wind = windSpeedMs * 3.6;   // convert to km/h
            wind = Math.round(wind * 100.0) / 100.0;
            long sunrise = root.path("sys").path("sunrise").asLong();
            long sunset = root.path("sys").path("sunset").asLong();
            String cityName = root.path("name").asText();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a")
                    .withZone(ZoneId.systemDefault());

            String sunriseTime = formatter.format(Instant.ofEpochSecond(sunrise));
            String sunsetTime = formatter.format(Instant.ofEpochSecond(sunset));

            model.addAttribute("city", cityName);
            model.addAttribute("description", description);
            model.addAttribute("temp", temp);
            model.addAttribute("humidity", humidity);
            model.addAttribute("description", description);
            model.addAttribute("wind", wind);
            model.addAttribute("sunrise", sunriseTime);
            model.addAttribute("sunset", sunsetTime);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error fetching weather data");
        }

        return "index";
    }

}
