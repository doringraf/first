public void highLightElement(WebElement element) {
        staticWait(100);
//        logger.info("Started Executing the Java Script ");
        JavascriptExecutor js = (JavascriptExecutor) Driver.getDriver();
        js.executeScript("arguments[0].setAttribute('style','background:yellow;border:2px solid red;');", element);
        try {
            Thread.sleep(1000);
//            logger.info("Executed and HighLighted");
        } catch (InterruptedException e) {
            System.out.print(e.getMessage());
        }
    }
