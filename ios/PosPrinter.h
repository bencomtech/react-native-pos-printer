
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNPosPrinterSpec.h"

@interface PosPrinter : NSObject <NativePosPrinterSpec>
#else
#import <React/RCTBridgeModule.h>

@interface PosPrinter : NSObject <RCTBridgeModule>
#endif

@end
