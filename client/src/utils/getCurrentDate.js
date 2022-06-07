export const getCurrentDate = () => {
  const currDate = new Date();
  const year = `${currDate.getFullYear()}`;
  let month = `${currDate.getMonth() + 1}`;

  if (month.length === 1) {
    month = '0' + month;
  }

  return year + month;
}