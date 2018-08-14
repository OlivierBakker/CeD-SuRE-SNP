package nl.umcg.suresnp.pipeline;


import htsjdk.samtools.*;
import nl.umcg.suresnp.pipeline.io.CSVReader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MergeBamWithBarcodes {

    private static final Logger LOGGER = Logger.getLogger(MergeBamWithBarcodes.class);

    public static void main(String[] args) {

        try {
            // Parse commandline arguments
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(MergeBamWithBarcodesParameters.getOptions(), args);

            File inputBam = new File(cmd.getOptionValue("i").trim());
            File inputBarcodes = new File(cmd.getOptionValue("b").trim());
            File outputFile = new File(cmd.getOptionValue("o").trim());


            CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(inputBarcodes))), "\t");

            Map<String, BarcodeReadPairs> barcodes = new HashMap<>();

            String[] line;
            long i = 0;
            long proper = 0;
            while ((line = reader.readNext()) != null) {

                if (i > 0) {
                    if (i % 1000000 == 0) {
                        LOGGER.info("Read " + i / 1000000 + " million records");
                    }
                }

                if (line.length != 11) {
                    //LOGGER.warn("Line not proper, skipping");
                    continue;
                } else {
                    if (Integer.parseInt(line[2]) == 20) {
                        proper ++;
                        Barcode curBarcode = new Barcode(line[0].split("\\s")[0], line[4], Integer.parseInt(line[2]));
                        if (barcodes.get(line[4]) != null) {
                            barcodes.get(line[4]).addBarcode(curBarcode);
                        } else {
                            barcodes.put(line[4], new BarcodeReadPairs(curBarcode));
                        }
                    }
                }
                i++;
            }

            LOGGER.info("Read " + proper + " valid barcode read pairs");
            LOGGER.info("Read " + barcodes.size() + " unique barcode read pairs");
            LOGGER.info(i - proper + " where invalid");

            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(outputFile));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));

            SamReader sr = SamReaderFactory.makeDefault().validationStringency(ValidationStringency.SILENT).open(inputBam);
            SAMRecordIterator r = sr.iterator();

            Map<String, SAMRecord> SamRecords = new HashMap<>();
            while(r.hasNext()) {
                SAMRecord record = r.next();
                SamRecords.put(record.getReadName(), record);
            }
            r.close();
            sr.close();
            reader.close();

            for (String barcodeId: barcodes.keySet()) {
                BarcodeReadPairs barcode = barcodes.get(barcodeId);
                writer.write(barcode.getBarcode());
                writer.write("\t");
                writer.write(Integer.toString(barcode.getDuplicateCount()));
                writer.newLine();
  //              writer.write("\t");
//                writer.write(SamRecords.get(barcode.getDuplicates().get(0).getReadId()).getReadName());
            }

            writer.flush();
            writer.close();


        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
