//
//  Database.m
//  TagTheBus
//
//  Created by Pascal Viguié on 07/12/2015.
//  Copyright © 2015 STUDIO Artaban. All rights reserved.
//

#import "Database.h"
#import "AlbumData.h"


static NSString* DB_FILE_NAME = @"Album.DB";

@interface Database ()
-(NSURL*)getDBFileURL;
-(BOOL)save;
@end

@implementation Database {
    NSMutableDictionary* DB;
}

//
-(BOOL)read {
    if (nil == DB)
        DB = [[NSMutableDictionary alloc] init];

    [DB removeAllObjects];
    NSURL* dbFile = [self getDBFileURL];

    NSError* err = nil;
    NSData* data = [NSData dataWithContentsOfURL:dbFile options:NSDataReadingUncached error:&err];
    if (nil == err)
        DB = (NSMutableDictionary*)[NSKeyedUnarchiver unarchiveObjectWithData:data];

    else {
    
        NSLog(@"ERROR: %@", err);
        return FALSE;
    }
    return TRUE;
}
-(BOOL)save {

    NSData* dataDB = [NSKeyedArchiver archivedDataWithRootObject:DB];
    NSURL* dbFile = [self getDBFileURL];
    NSError* err = nil;
    if ([dataDB writeToURL:dbFile options:NSDataWritingAtomic error:&err] == NO) {

        NSLog(@"ERROR: %@", err);
        return FALSE;
    }
    return TRUE;
}

-(BOOL)add:(NSDictionary*)photo toAlbum:(int)StationID {

    NSMutableArray* album = [self getAlbum:StationID];
    if (nil == album) {

        NSMutableDictionary* newAlbum = [[NSMutableDictionary alloc] init];
        newAlbum[ALBUM_ID] = [@(StationID) stringValue];
        newAlbum[ALBUM_PHOTOS] = [[NSMutableArray alloc] initWithObjects:photo, nil];

        if (nil == [DB valueForKey:ALBUM_DB])
            DB[ALBUM_DB] = [[NSMutableArray alloc] init];
        
        [[DB valueForKey:ALBUM_DB] addObject:newAlbum];
    }
    else
        [album addObject:photo];

    return [self save];
}

-(NSURL*)getDBFileURL {

    NSArray* paths = [[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask];
    NSURL* documents = [paths objectAtIndex:0];
    return [documents URLByAppendingPathComponent:DB_FILE_NAME];
}

-(NSMutableArray*)getAlbum:(int)ID {

    NSMutableArray* albums = [DB valueForKey:ALBUM_DB];
    for (NSMutableDictionary* album in albums)
        if ([(NSString*)[album valueForKey:ALBUM_ID] intValue] == ID)
            return [album valueForKey:ALBUM_PHOTOS];

    return nil;
}

@end
