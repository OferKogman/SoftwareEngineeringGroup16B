
import { BrowserRouter, Route, Routes } from "react-router-dom";
import ViewDiscountPolicy, {
  type DiscountPolicyData,
} from "./Components/CreateDiscountPolicy";
import EventCreationForm, {
  type EventCreationData,
} from "./Components/EventCreationForm";
import EventUpdateForm, {
  type EventUpdateDetails,
} from "./Components/EventUpdateForm";
import LoginForm, { type LoginData } from "./Components/LoginFrom";
import PaymentForm from "./Components/PaymentForm";
import CreateProductionCompany, {
  type ProductionCompanyDTO,
} from "./Components/ProdctionCompanyForm";
import PurchasePolicyCreationForm, {
  type PurchasePolicyCreationData,
} from "./Components/PurchasePolicyCreationForm";
import RegistrationForm from "./Components/RegistrationForm";
import SearchEvents from "./Components/SearchEvents";
import VenueEditor from "./Components/VenueEditor";
import ViewCompanyEvents from "./Components/ViewCompanyEventList";
import ViewEvent from "./Components/ViewEvent";
import ViewOrder from "./Components/Shered/ViewOrder";
import { useEffect, useState } from "react";
import AssignMember from "./Components/AssignNewOwnerOrManager";
import ChangeManagerPermissions, {
  type ManagerPermissions,
} from "./Components/ChangeManagerPermissions";
import CreateOrderPage from "./Components/CreateOrder";
import ViewAdminCompanyList from "./Components/ViewAdminCompanyList";
import AdminPurchaseHistory from "./Components/ViewAdminPurcheseHistory";
import ProductionCompanyPurchaseHistory from "./Components/ProdactionCompany/ViewProductionCompanyPurchaseHistory";
import ViewUserCompanyList from "./Components/ViewUserCompanyList";
import UserPurchaseHistory from "./Components/ViewUserPurchaseHistory";
import ViewUsers from "./Components/ViewUsersList";
import AppRoutes from "./AppRoutes";

import { useEffect, useState } from "react";
import Header from "./Components/Layouts/Header";
>>>>>>> origin/main
import "./CSS/App.css";

function App() {
  const [theme, setTheme] = useState(localStorage.getItem("theme") || "light");
  useEffect(() => {
    localStorage.setItem("theme", theme);
  }, [theme]);

  return (
    <BrowserRouter>
      <div className={`app ${theme}`}>
        <Header theme={theme} setTheme={setTheme} />
        <AppRoutes />
      </div>
    </BrowserRouter>
  );
}
export default App;
