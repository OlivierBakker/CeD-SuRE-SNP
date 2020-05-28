package nl.umcg.suresnp.pipeline.records.ipcrrecord.filters;


public enum IpcrRecordFilterType {

    IN_REGION,
    ANY_BC_GT_EQ,
    ANY_BC_ST_EQ;

    /**
     * Create filter.
     *
     * @param arg the arg
     * @return the ipcr record filter
     */
    public static IpcrRecordFilter createFilter(String arg) {
        String[] filters = arg.trim().split(";");
        String filterType = filters[0];
        String args = filters[1];
        System.out.println(arg);
        System.out.println(args);
        String[] splitArgs;
        int count;
        boolean invert;

        try {
            switch (IpcrRecordFilterType.valueOf(filterType)) {
                case IN_REGION:
                    String sequenceName = args.split(":")[0];
                    String region = args.split(":")[1];
                    int lower = Integer.parseInt(region.split("-")[0]);
                    int upper = Integer.parseInt(region.split("-")[1]);
                    return new InRegionFilter(sequenceName, lower, upper);
                case ANY_BC_GT_EQ:
                    splitArgs = args.split(":");
                    count = Integer.parseInt(splitArgs[0]);
                    invert = splitArgs[1].toUpperCase().equals("TRUE");
                    return new AnyBarcodeCountGreaterEqualsFilter(count, invert);
                case ANY_BC_ST_EQ:
                    splitArgs = args.split(":");
                    count = Integer.parseInt(splitArgs[0]);
                    invert = splitArgs[1].toUpperCase().equals("TRUE");
                    return new AnyBarcodeCountSmallerEqualsFilter(count, invert);

            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid filter type specified: " + filterType);
        }

        return null;
    }

}
