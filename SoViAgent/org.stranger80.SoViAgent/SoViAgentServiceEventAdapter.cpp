//-----------------------------------------------------------------------------
// <auto-generated> 
//   This code was generated by a tool. 
// 
//   Changes to this file may cause incorrect behavior and will be lost if  
//   the code is regenerated.
//
//   Tool: AllJoynCodeGenerator.exe
//
//   This tool is located in the Windows 10 SDK and the Windows 10 AllJoyn 
//   Visual Studio Extension in the Visual Studio Gallery.  
//
//   The generated code should be packaged in a Windows 10 C++/CX Runtime  
//   Component which can be consumed in any UWP-supported language using 
//   APIs that are available in Windows.Devices.AllJoyn.
//
//   Using AllJoynCodeGenerator - Invoke the following command with a valid 
//   Introspection XML file and a writable output directory:
//     AllJoynCodeGenerator -i <INPUT XML FILE> -o <OUTPUT DIRECTORY>
// </auto-generated>
//-----------------------------------------------------------------------------
#include "pch.h"

using namespace Microsoft::WRL;
using namespace Platform;
using namespace Windows::Foundation;
using namespace Windows::Foundation::Collections;
using namespace Windows::Devices::AllJoyn;
using namespace org::stranger80::SoViAgent;

// Note: Unlike an Interface implementation, which provides a single handler for each member, the event
// model allows for 0 or more listeners to be registered. The EventAdapter implementation deals with this
// difference by implementing a last-writer-wins policy. The lack of any return value (i.e., 0 listeners)
// is handled by returning a null result.

// Methods
IAsyncOperation<SoViAgentShutdownResult^>^ SoViAgentServiceEventAdapter::ShutdownAsync(_In_ AllJoynMessageInfo^ info)
{
    auto args = ref new SoViAgentShutdownCalledEventArgs(info);
    ShutdownCalled(this, args);
    return SoViAgentShutdownCalledEventArgs::GetResultAsync(args);
}

IAsyncOperation<SoViAgentExecuteHeatPatternResult^>^ SoViAgentServiceEventAdapter::ExecuteHeatPatternAsync(_In_ AllJoynMessageInfo^ info, _In_ Windows::Foundation::Collections::IVectorView<SoViAgentHeatPointArrayItem^>^ interfaceMemberHeatPointArray)
{
    auto args = ref new SoViAgentExecuteHeatPatternCalledEventArgs(info, interfaceMemberHeatPointArray);
    ExecuteHeatPatternCalled(this, args);
    return SoViAgentExecuteHeatPatternCalledEventArgs::GetResultAsync(args);
}

IAsyncOperation<SoViAgentGetStateHistoryResult^>^ SoViAgentServiceEventAdapter::GetStateHistoryAsync(_In_ AllJoynMessageInfo^ info, _In_ uint32 interfaceMemberStartTime)
{
    auto args = ref new SoViAgentGetStateHistoryCalledEventArgs(info, interfaceMemberStartTime);
    GetStateHistoryCalled(this, args);
    return SoViAgentGetStateHistoryCalledEventArgs::GetResultAsync(args);
}

// Property Reads
IAsyncOperation<SoViAgentGetVersionResult^>^ SoViAgentServiceEventAdapter::GetVersionAsync(_In_ AllJoynMessageInfo^ info)
{
    auto args = ref new SoViAgentGetVersionRequestedEventArgs(info);
    GetVersionRequested(this, args);
    return SoViAgentGetVersionRequestedEventArgs::GetResultAsync(args);
}

IAsyncOperation<SoViAgentGetTemperatureResult^>^ SoViAgentServiceEventAdapter::GetTemperatureAsync(_In_ AllJoynMessageInfo^ info)
{
    auto args = ref new SoViAgentGetTemperatureRequestedEventArgs(info);
    GetTemperatureRequested(this, args);
    return SoViAgentGetTemperatureRequestedEventArgs::GetResultAsync(args);
}

IAsyncOperation<SoViAgentGetHeaterResult^>^ SoViAgentServiceEventAdapter::GetHeaterAsync(_In_ AllJoynMessageInfo^ info)
{
    auto args = ref new SoViAgentGetHeaterRequestedEventArgs(info);
    GetHeaterRequested(this, args);
    return SoViAgentGetHeaterRequestedEventArgs::GetResultAsync(args);
}

IAsyncOperation<SoViAgentGetCirculationResult^>^ SoViAgentServiceEventAdapter::GetCirculationAsync(_In_ AllJoynMessageInfo^ info)
{
    auto args = ref new SoViAgentGetCirculationRequestedEventArgs(info);
    GetCirculationRequested(this, args);
    return SoViAgentGetCirculationRequestedEventArgs::GetResultAsync(args);
}

// Property Writes
