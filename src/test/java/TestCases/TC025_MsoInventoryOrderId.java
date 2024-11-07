package TestCases;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import Utilities.AppUtils;
import Utilities.ExcelUtils;
import Utilities.ExtentTestNGListener;
import com.aventstack.extentreports.Status;

@Listeners(ExtentTestNGListener.class)
public class TC025_MsoInventoryOrderId extends AppUtils {

    @Test
    public void msoInventoryOrderIdTest() throws Throwable {
        // Start logging in Extent Report
        ExtentTestNGListener.getExtentTest().log(Status.INFO, "Test Case: msoInventoryOrderIdTest started");

        // Open the application URL
        driver.get(url);
        ExtentTestNGListener.getExtentTest().log(Status.INFO, "Navigated to URL: " + url);

        // Perform MSO login
        msoLogin();
        ExtentTestNGListener.getExtentTest().log(Status.INFO, "MSO login performed");

        // Define row and column numbers for test data and smoke testing results
        int smokeTestRowNum = 31;
        int smokeTestColNum = 12;
        int smokeTestTimestampColNum = 15;

        // Navigate to Inventory Page
        driver.findElement(By.xpath("//span[normalize-space()='Admin Inventory']")).click();
        ExtentTestNGListener.getExtentTest().log(Status.INFO, "Clicked on Admin Inventory");
        driver.findElement(By.xpath("//button[normalize-space()='Orders']")).click();
        ExtentTestNGListener.getExtentTest().log(Status.INFO, "Clicked on Orders button");
        Thread.sleep(1000);

        // Verify Orders Request page is displayed
        WebElement verifyOrdersRequest = driver.findElement(By.xpath("//p[contains(@class, 'text-base') and contains(text(), 'Orders Request')]"));
        boolean isOrdersRequestVisible = verifyOrdersRequest.getText().toLowerCase().equals("orders request");
        System.out.println("Orders Request visibility in Inventory page: " + isOrdersRequestVisible);
        System.out.println("Text found: " + verifyOrdersRequest.getText());
        Assert.assertTrue(isOrdersRequestVisible, "Orders Request was not visible after clicking Admin Inventory for orders");
        ExtentTestNGListener.getExtentTest().log(Status.PASS, "Orders Request page is visible");

        // Define the value to be tested
        String expectedValue = "DORde4b2d7";
        boolean valueFound = false;

        // Pagination and data extraction loop
        while (!valueFound) {
            try {
                // Locate the table containing the Orders Request data
                WebElement ordersTable = driver.findElement(By.xpath("//table[@class='w-full table-data']"));
                List<WebElement> rows = ordersTable.findElements(By.tagName("tr"));

                // Find the index of the "Order ID" column
                List<WebElement> headers = ordersTable.findElements(By.tagName("th"));
                int orderIdColumnIndex = 0;
                for (int i = 0; i < headers.size(); i++) {
                    if (headers.get(i).getText().trim().equalsIgnoreCase("Order ID")) {
                        orderIdColumnIndex = i;
                        break;
                    }
                }

                // Iterate through each row to find the specified value
                for (WebElement row : rows) {
                    List<WebElement> columns = row.findElements(By.tagName("td"));

                    if (columns.size() > 0) {
                        String cellText = columns.get(orderIdColumnIndex).getText().trim();
                        if (cellText.equals(expectedValue)) {
                            valueFound = true;
                            System.out.println("Order ID found: " + cellText);
                            ExtentTestNGListener.getExtentTest().log(Status.PASS, "Order ID found: " + cellText);
                            break;
                        }
                    }
                }

                if (!valueFound) {
                    try {
                        // Locate the pagination next button and click it
                        WebElement nextButton = driver.findElement(By.xpath("(//button[contains(@class,'h-[30px] w-[30px] m-1 text-sm font-medium rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500')][normalize-space()='>'])[1]"));
                        if (nextButton.isEnabled()) {
                            nextButton.click();
                            //ExtentTestNGListener.getExtentTest().log(Status.INFO, "Clicked on Next button for pagination");
                            Thread.sleep(3000); // Wait for the next page to load
                        } else {
                            // No more pages left
                            break; // Exit the loop if no more pages are available
                        }
                    } catch (NoSuchElementException e) {
                        // 'Next' button not found, meaning no additional pages exist
                        ExtentTestNGListener.getExtentTest().log(Status.INFO, "No Next button found, last page reached");
                        break; // Exit the loop as we are on the last page
                    }
                }

            } catch (NoSuchElementException e) {
                // Handle potential issues with finding elements
                ExtentTestNGListener.getExtentTest().log(Status.FAIL, "Error encountered: " + e.getMessage());
                System.out.println("Error encountered: " + e.getMessage());
                break;
            }
        }

        // Assert that the expected value was found in the table
        Assert.assertTrue(valueFound, "The expected Order ID 'DORde4b2d7' was not found.");
        ExtentTestNGListener.getExtentTest().log(Status.PASS, "The expected Order ID 'DORde4b2d7' was found.");

        // Update summary results in the 'Smoke Test Cases' sheet
        if (valueFound && isOrdersRequestVisible) {
            // Update result as 'Pass' in the Excel sheet
            ExcelUtils.setCellData(smokeTestingFilePath, smokeTestingSheetName, smokeTestRowNum, smokeTestColNum, "Pass");
            ExcelUtils.fillGreenColor(smokeTestingFilePath, smokeTestingSheetName, smokeTestRowNum, smokeTestColNum);
            ExtentTestNGListener.getExtentTest().log(Status.PASS, "Test results updated in Excel with 'Pass' status");

            // Log timestamp of the test execution
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String formattedNow = now.format(formatter);
            ExcelUtils.setCellData(smokeTestingFilePath, smokeTestingSheetName, smokeTestRowNum, smokeTestTimestampColNum, formattedNow);
            ExtentTestNGListener.getExtentTest().log(Status.INFO, "Test results updated in Excel with timestamp: " + formattedNow);
        } else {
            // Log failure in Extent Reports if validation fails
            ExtentTestNGListener.getExtentTest().log(Status.FAIL, "Order ID or Orders Request visibility validation failed");
        }
    }
}
