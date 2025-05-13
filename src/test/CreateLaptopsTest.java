package test;

import client.network.SocketClient;
import client.network.SocketClientImp;
import enums.PerformanceTypeEnum;
import objects.Laptop;
import org.junit.*;
import server.model.ServerModel;
import server.model.ServerModelImpl;
import server.network.SocketServer;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Test der opretter 10 forskellige laptops.
 * Kræver at serveren kører eller at testen starter sin egen server.
 */
public class CreateLaptopsTest {
    private static SocketServer server;
    private static ServerModel serverModel;
    private static final int TEST_PORT = 8885;
    private SocketClient client;
    private Random random = new Random();

    @BeforeClass
    public static void setUpClass() {
        try {
            // Start server for testen
            serverModel = new ServerModelImpl();
            server = new SocketServer(serverModel, TEST_PORT);
            server.startServer();
            Thread.sleep(1000); // Vent på at serveren starter
        } catch (Exception e) {
            fail("Kunne ikke starte server: " + e.getMessage());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        if (server != null && server.isRunning()) {
            server.stopServer();
        }
    }

    @Before
    public void setUp() {
        try {
            // Opret klient forbindelse
            client = new SocketClientImp("localhost", TEST_PORT);
            Thread.sleep(500); // Vent på forbindelse
        } catch (Exception e) {
            fail("Kunne ikke opsætte test: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        if (client instanceof SocketClientImp) {
            ((SocketClientImp) client).disconnect();
        }
    }

    @Test
    public void testCreate10Laptops() {
        // Forskellige laptop mærker og modeller til variation
        String[] brands = {"Dell", "HP", "Lenovo", "Apple", "Asus"};
        String[] highModels = {"Precision 7760", "ZBook Fury", "ThinkPad P1", "MacBook Pro", "ProArt StudioBook"};
        String[] lowModels = {"Latitude 3420", "ProBook 450", "ThinkPad E15", "MacBook Air", "VivoBook"};

        int[] ramOptions = {8, 16, 32, 64};
        int[] diskOptions = {256, 512, 1024, 2048};

        // Opret 10 laptops (5 høj-ydelses og 5 lav-ydelses)
        for (int i = 0; i < 10; i++) {
            boolean isHighPerformance = i < 5;

            String brand = brands[random.nextInt(brands.length)];
            String model = isHighPerformance ?
                    highModels[random.nextInt(highModels.length)] :
                    lowModels[random.nextInt(lowModels.length)];

            int ram = isHighPerformance ?
                    ramOptions[2 + random.nextInt(2)] : // 32 eller 64 GB for høj-ydelses
                    ramOptions[random.nextInt(2)];      // 8 eller 16 GB for lav-ydelses

            int disk = isHighPerformance ?
                    diskOptions[2 + random.nextInt(2)] : // 1024 eller 2048 GB for høj-ydelses
                    diskOptions[random.nextInt(2)];      // 256 eller 512 GB for lav-ydelses

            PerformanceTypeEnum performanceType = isHighPerformance ?
                    PerformanceTypeEnum.HIGH :
                    PerformanceTypeEnum.LOW;

            try {
                // Opret laptop
                Laptop laptop = client.createLaptop(brand, model, disk, ram, performanceType);

                // Verificer at laptop blev oprettet
                assertNotNull("Laptop #" + (i + 1) + " skulle være oprettet", laptop);
                assertEquals("Brand matcher", brand, laptop.getBrand());
                assertEquals("Model matcher", model, laptop.getModel());
                assertEquals("RAM matcher", ram, laptop.getRam());
                assertEquals("Disk matcher", disk, laptop.getGigabyte());
                assertEquals("Performance type matcher", performanceType, laptop.getPerformanceType());
                assertTrue("Laptop skal være tilgængelig", laptop.isAvailable());

                System.out.println("Oprettet laptop #" + (i + 1) + ": " +
                        laptop.getBrand() + " " + laptop.getModel() +
                        " (" + laptop.getRam() + "GB RAM, " + laptop.getGigabyte() + "GB Disk, " +
                        laptop.getPerformanceType() + ")");

                // Vent lidt mellem oprettelser for at undgå at overloade systemet
                Thread.sleep(100);

            } catch (Exception e) {
                fail("Fejl ved oprettelse af laptop #" + (i + 1) + ": " + e.getMessage());
            }
        }

        // Verificer at alle 10 laptops er i systemet
        List<Laptop> allLaptops = client.getAllLaptops();
        assertTrue("Der skal være mindst 10 laptops i systemet", allLaptops.size() >= 10);

        System.out.println("\nTotal antal laptops i systemet: " + allLaptops.size());
    }

    @Test
    public void testCreate10LaptopsWithSpecificData() {
        // Mere struktureret test med specifikke laptop konfigurationer
        Object[][] laptopConfigs = {
                // Brand, Model, Disk, RAM, PerformanceType
                {"Dell", "Precision 7760", 2048, 64, PerformanceTypeEnum.HIGH},
                {"HP", "ZBook Fury 15", 1024, 32, PerformanceTypeEnum.HIGH},
                {"Lenovo", "ThinkPad P15", 2048, 64, PerformanceTypeEnum.HIGH},
                {"Apple", "MacBook Pro 16", 1024, 32, PerformanceTypeEnum.HIGH},
                {"Asus", "ProArt StudioBook", 2048, 64, PerformanceTypeEnum.HIGH},
                {"Dell", "Latitude 3420", 256, 8, PerformanceTypeEnum.LOW},
                {"HP", "ProBook 450 G8", 512, 16, PerformanceTypeEnum.LOW},
                {"Lenovo", "ThinkPad E15", 256, 8, PerformanceTypeEnum.LOW},
                {"Apple", "MacBook Air", 512, 16, PerformanceTypeEnum.LOW},
                {"Asus", "VivoBook 15", 256, 8, PerformanceTypeEnum.LOW}
        };

        for (int i = 0; i < laptopConfigs.length; i++) {
            Object[] config = laptopConfigs[i];

            String brand = (String) config[0];
            String model = (String) config[1];
            int disk = (int) config[2];
            int ram = (int) config[3];
            PerformanceTypeEnum performanceType = (PerformanceTypeEnum) config[4];

            Laptop laptop = client.createLaptop(brand, model, disk, ram, performanceType);

            assertNotNull("Laptop #" + (i + 1) + " skulle være oprettet", laptop);
            System.out.println("Oprettet: " + laptop);
        }
    }
}