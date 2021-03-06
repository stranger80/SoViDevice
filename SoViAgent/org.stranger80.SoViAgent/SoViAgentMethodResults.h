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
#pragma once

using namespace concurrency;

namespace org { namespace stranger80 { namespace SoViAgent {

ref class SoViAgentConsumer;

public ref class SoViAgentShutdownResult sealed
{
public:
    property int32 Status
    {
        int32 get() { return m_status; }
    internal:
        void set(_In_ int32 value) { m_status = value; }
    }

    property bool Success
    {
        bool get() { return m_interfaceMemberSuccess; }
    internal:
        void set(_In_ bool value) { m_interfaceMemberSuccess = value; }
    }
    
    static SoViAgentShutdownResult^ CreateSuccessResult(_In_ bool interfaceMemberSuccess)
    {
        auto result = ref new SoViAgentShutdownResult();
        result->Status = Windows::Devices::AllJoyn::AllJoynStatus::Ok;
        result->Success = interfaceMemberSuccess;
        result->m_creationContext = Concurrency::task_continuation_context::use_current();
        return result;
    }
    
    static SoViAgentShutdownResult^ CreateFailureResult(_In_ int32 status)
    {
        auto result = ref new SoViAgentShutdownResult();
        result->Status = status;
        return result;
    }
internal:
    Concurrency::task_continuation_context m_creationContext = Concurrency::task_continuation_context::use_default();

private:
    int32 m_status;
    bool m_interfaceMemberSuccess;
};

public ref class SoViAgentExecuteHeatPatternResult sealed
{
public:
    property int32 Status
    {
        int32 get() { return m_status; }
    internal:
        void set(_In_ int32 value) { m_status = value; }
    }

    property bool Success
    {
        bool get() { return m_interfaceMemberSuccess; }
    internal:
        void set(_In_ bool value) { m_interfaceMemberSuccess = value; }
    }
    
    static SoViAgentExecuteHeatPatternResult^ CreateSuccessResult(_In_ bool interfaceMemberSuccess)
    {
        auto result = ref new SoViAgentExecuteHeatPatternResult();
        result->Status = Windows::Devices::AllJoyn::AllJoynStatus::Ok;
        result->Success = interfaceMemberSuccess;
        result->m_creationContext = Concurrency::task_continuation_context::use_current();
        return result;
    }
    
    static SoViAgentExecuteHeatPatternResult^ CreateFailureResult(_In_ int32 status)
    {
        auto result = ref new SoViAgentExecuteHeatPatternResult();
        result->Status = status;
        return result;
    }
internal:
    Concurrency::task_continuation_context m_creationContext = Concurrency::task_continuation_context::use_default();

private:
    int32 m_status;
    bool m_interfaceMemberSuccess;
};

public ref class SoViAgentGetStateHistoryResult sealed
{
public:
    property int32 Status
    {
        int32 get() { return m_status; }
    internal:
        void set(_In_ int32 value) { m_status = value; }
    }

    property Windows::Foundation::Collections::IVector<SoViAgentSuccessItem^>^ Success
    {
        Windows::Foundation::Collections::IVector<SoViAgentSuccessItem^>^ get() { return m_interfaceMemberSuccess; }
    internal:
        void set(_In_ Windows::Foundation::Collections::IVector<SoViAgentSuccessItem^>^ value) { m_interfaceMemberSuccess = value; }
    }
    
    static SoViAgentGetStateHistoryResult^ CreateSuccessResult(_In_ Windows::Foundation::Collections::IVector<SoViAgentSuccessItem^>^ interfaceMemberSuccess)
    {
        auto result = ref new SoViAgentGetStateHistoryResult();
        result->Status = Windows::Devices::AllJoyn::AllJoynStatus::Ok;
        result->Success = interfaceMemberSuccess;
        result->m_creationContext = Concurrency::task_continuation_context::use_current();
        return result;
    }
    
    static SoViAgentGetStateHistoryResult^ CreateFailureResult(_In_ int32 status)
    {
        auto result = ref new SoViAgentGetStateHistoryResult();
        result->Status = status;
        return result;
    }
internal:
    Concurrency::task_continuation_context m_creationContext = Concurrency::task_continuation_context::use_default();

private:
    int32 m_status;
    Windows::Foundation::Collections::IVector<SoViAgentSuccessItem^>^ m_interfaceMemberSuccess;
};

public ref class SoViAgentJoinSessionResult sealed
{
public:
    property int32 Status
    {
        int32 get() { return m_status; }
    internal:
        void set(_In_ int32 value) { m_status = value; }
    }

    property SoViAgentConsumer^ Consumer
    {
        SoViAgentConsumer^ get() { return m_consumer; }
    internal:
        void set(_In_ SoViAgentConsumer^ value) { m_consumer = value; }
    };

private:
    int32 m_status;
    SoViAgentConsumer^ m_consumer;
};

public ref class SoViAgentGetVersionResult sealed
{
public:
    property int32 Status
    {
        int32 get() { return m_status; }
    internal:
        void set(_In_ int32 value) { m_status = value; }
    }

    property uint16 Version
    {
        uint16 get() { return m_value; }
    internal:
        void set(_In_ uint16 value) { m_value = value; }
    }

    static SoViAgentGetVersionResult^ CreateSuccessResult(_In_ uint16 value)
    {
        auto result = ref new SoViAgentGetVersionResult();
        result->Status = Windows::Devices::AllJoyn::AllJoynStatus::Ok;
        result->Version = value;
        result->m_creationContext = Concurrency::task_continuation_context::use_current();
        return result;
    }

    static SoViAgentGetVersionResult^ CreateFailureResult(_In_ int32 status)
    {
        auto result = ref new SoViAgentGetVersionResult();
        result->Status = status;
        return result;
    }
internal:
    Concurrency::task_continuation_context m_creationContext = Concurrency::task_continuation_context::use_default();

private:
    int32 m_status;
    uint16 m_value;
};

public ref class SoViAgentGetTemperatureResult sealed
{
public:
    property int32 Status
    {
        int32 get() { return m_status; }
    internal:
        void set(_In_ int32 value) { m_status = value; }
    }

    property double Temperature
    {
        double get() { return m_value; }
    internal:
        void set(_In_ double value) { m_value = value; }
    }

    static SoViAgentGetTemperatureResult^ CreateSuccessResult(_In_ double value)
    {
        auto result = ref new SoViAgentGetTemperatureResult();
        result->Status = Windows::Devices::AllJoyn::AllJoynStatus::Ok;
        result->Temperature = value;
        result->m_creationContext = Concurrency::task_continuation_context::use_current();
        return result;
    }

    static SoViAgentGetTemperatureResult^ CreateFailureResult(_In_ int32 status)
    {
        auto result = ref new SoViAgentGetTemperatureResult();
        result->Status = status;
        return result;
    }
internal:
    Concurrency::task_continuation_context m_creationContext = Concurrency::task_continuation_context::use_default();

private:
    int32 m_status;
    double m_value;
};

public ref class SoViAgentGetHeaterResult sealed
{
public:
    property int32 Status
    {
        int32 get() { return m_status; }
    internal:
        void set(_In_ int32 value) { m_status = value; }
    }

    property int32 Heater
    {
        int32 get() { return m_value; }
    internal:
        void set(_In_ int32 value) { m_value = value; }
    }

    static SoViAgentGetHeaterResult^ CreateSuccessResult(_In_ int32 value)
    {
        auto result = ref new SoViAgentGetHeaterResult();
        result->Status = Windows::Devices::AllJoyn::AllJoynStatus::Ok;
        result->Heater = value;
        result->m_creationContext = Concurrency::task_continuation_context::use_current();
        return result;
    }

    static SoViAgentGetHeaterResult^ CreateFailureResult(_In_ int32 status)
    {
        auto result = ref new SoViAgentGetHeaterResult();
        result->Status = status;
        return result;
    }
internal:
    Concurrency::task_continuation_context m_creationContext = Concurrency::task_continuation_context::use_default();

private:
    int32 m_status;
    int32 m_value;
};

public ref class SoViAgentGetCirculationResult sealed
{
public:
    property int32 Status
    {
        int32 get() { return m_status; }
    internal:
        void set(_In_ int32 value) { m_status = value; }
    }

    property int32 Circulation
    {
        int32 get() { return m_value; }
    internal:
        void set(_In_ int32 value) { m_value = value; }
    }

    static SoViAgentGetCirculationResult^ CreateSuccessResult(_In_ int32 value)
    {
        auto result = ref new SoViAgentGetCirculationResult();
        result->Status = Windows::Devices::AllJoyn::AllJoynStatus::Ok;
        result->Circulation = value;
        result->m_creationContext = Concurrency::task_continuation_context::use_current();
        return result;
    }

    static SoViAgentGetCirculationResult^ CreateFailureResult(_In_ int32 status)
    {
        auto result = ref new SoViAgentGetCirculationResult();
        result->Status = status;
        return result;
    }
internal:
    Concurrency::task_continuation_context m_creationContext = Concurrency::task_continuation_context::use_default();

private:
    int32 m_status;
    int32 m_value;
};

} } } 
