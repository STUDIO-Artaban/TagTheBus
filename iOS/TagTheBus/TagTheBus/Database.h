//
//  Database.h
//  TagTheBus
//
//  Created by Pascal Viguié on 07/12/2015.
//  Copyright © 2015 STUDIO Artaban. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Database : NSObject

-(BOOL)read;
-(BOOL)add:(NSDictionary*)photo toAlbum:(int)StationID;

-(NSMutableArray*)getAlbum:(int)ID;

@end
