import { ImageBackground,StyleSheet, Text, View } from 'react-native';
import React, { useState, useEffect } from 'react';
import { Accelerometer } from 'expo-sensors';

export default function App() {

  const wait = 500;
  //Save x,y,z values
  const [data, setData] = useState({
    x:0, y:0, z:0,
  });

  const [steps, setSteps] = useState(0);

  // Create a subscription for the porcess
  const [subscription, setSubscription] = useState(null);

  //Speed to obtain x,y,z values
  const _slow = () => {
    Accelerometer.setUpdateInterval(wait);
  };

  const _subscribe = () => {
    setSubscription(
      Accelerometer.addListener(accelerometerData =>{
        setData(accelerometerData);
      })
    );
  };

  useEffect( () => {
    _subscribe();
    _slow();
    const stepammount = setInterval (() => {

      const {x,y,z} = data;
      const Acceleration = Math.sqrt((x * x) + (y * y) + (z * z),2);
      if (Acceleration > 1.10){

        setSteps(data+1);
      };
    },wait);
    return () =>clearTimeout(stepammount);
  },[]);

  return (

    <View style={styles.container}>
      <ImageBackground 
        source={require('./assets/background.png')}
        resizeMode ='cover'
        style={{flex : 1}}>

        <View style = {styles.counter}>
          <Text style = {styles.counterText2}>{steps}</Text>
          <Text style = {styles.normalFont}>Steps</Text>
        </View>
        </ImageBackground>
    </View>
  );
}


const styles = StyleSheet.create({
  container: {
    flex:1,
  },
  counter:{
    flex:1,
    justifyContent: 'center',
  },
  counterText:{
    color: 'white',
    alignSelf: 'center',
    fontSize:20,
    fontWeight: 'bold',
  },
  counterText2:{
    color: 'white',
    alignSelf: 'center',
    fontSize:130,
    fontWeight: 'bold',
  },
  normalFont:{
    color:'white',
    alignSelf:'center',
   fontSize:70,
  },
  smallFont:{
    color:'white',
    alignSelf:'center',
   fontSize:12,
  }
});
