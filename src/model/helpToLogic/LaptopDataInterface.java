package model.helpToLogic;

import enums.PerformanceTypeEnum;
import objects.Laptop;
import objects.LaptopState;

import java.util.List;
import java.util.UUID;

public interface LaptopDataInterface {
    List<Laptop> getAllLaptops();
    List<Laptop> getAvailableLaptops();
    List<Laptop> getLoanedLaptops();
    Laptop seeNextAvailableLaptop(PerformanceTypeEnum performanceTypeEnum);
    Laptop getLaptopByUUID(UUID id);
    Laptop createLaptop(String brand, String model, int gigabyte, int ram, PerformanceTypeEnum performanceType);
    Laptop updateLaptopState(UUID id);
    Laptop deleteLaptop(UUID id);
}
