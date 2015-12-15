//
//  StationData.h
//  TagTheBus
//
//  Created by Pascal Viguié on 25/11/2015.
//  Copyright (c) 2015 STUDIO Artaban. All rights reserved.
//

#ifndef TagTheBus_StationData_h
#define TagTheBus_StationData_h


static NSString* STATION_DATA_URL = @"http://barcelonaapi.marcpous.com/bus/nearstation/latlon/41.3985182/2.1917991/1.json";

// JSON stations struct
static NSString* JSON_DATA = @"data";
static NSString* JSON_STATIONS = @"nearstations";

static NSString* STATION_ID = @"id";
static NSString* STATION_STREET = @"street_name";
static NSString* STATION_BUSES = @"buses";
static NSString* STATION_LATITUDE = @"lat";
static NSString* STATION_LONGITUDE = @"lon";

#endif
