package rest.bef.demo.data.dto;

public class ReportDTO {
    
    private Double sum;
    private Double avg;
    private Double stdd;

    public ReportDTO() {
    }

    public ReportDTO(Double sum, Double avg, Double stdd) {
        this.sum = sum;
        this.avg = avg;
        this.stdd = stdd;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
        this.avg = avg;
    }

    public Double getStdd() {
        return stdd;
    }

    public void setStdd(Double stdd) {
        this.stdd = stdd;
    }
}
