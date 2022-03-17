package account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


public class PaymentResponseDto {
    private String name;
    private String lastname;
    private YearMonth period;
    private String salary;

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM-yyyy", Locale.US);

    public PaymentResponseDto(String name, String lastname, YearMonth period, String salary) {
        this.name = name;
        this.lastname = lastname;
        this.period = period;
        this.salary = salary;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPeriod() {
        return  period.format(formatter);
    }

    public YearMonth periodYearMonth() {
        return  period;
    }

    public String getSalary() {
        return salary;
    }
}
