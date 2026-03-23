package taskmanager.model.interfaces;

public interface CsvSerializable {
    String toCsv();
    void fromCsv(String row);
}