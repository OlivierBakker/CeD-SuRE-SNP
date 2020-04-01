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

        try {
            switch (IpcrRecordFilterType.valueOf(filterType)) {
                case IN_REGION:
                    String sequenceName = args.split(":")[0];
                    String region = args.split(":")[1];
                    int lower = Integer.parseInt(region.split("-")[0]);
                    int upper = Integer.parseInt(region.split("-")[1]);
                    return new InRegionFilter(sequenceName, lower, upper);
                case ANY_BC_GT_EQ:
                    int count = Integer.parseInt(args);
                    return new AnyBarcodeCountGreaterEqualsFilter(count);
                case ANY_BC_ST_EQ:
                    int count2 = Integer.parseInt(args);
                    return new AnyBarcodeCountSmallerEqualsFilter(count2);

            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid filter type specified: " + filterType);
        }

        return null;
    }

}
