import './App.css';
import EnergyChart from './components/EnergyChart';
import Header from './components/Header';
import MapArea from './components/MapArea';

function App() {
  return (
    <div className="App">
      <Header />
      <EnergyChart />
      <MapArea />
    </div>
  );
}

export default App;
