type TableColumn = {
  key: string;
  label: string;
};

type TableProps<T extends Record<string, unknown>> = {
  columns: TableColumn[];
  rows: T[];
};

function Table<T extends Record<string, unknown>>({ columns, rows }: TableProps<T>) {
  return (
    <div style={{ width: '100%', overflowX: 'auto' }}>
      <table
        style={{
          width: '100%',
          borderCollapse: 'collapse',
          borderSpacing: 0,
        }}
      >
        <thead>
          <tr>
            {columns.map((column) => (
              <th
                key={column.key}
                style={{
                  textAlign: 'left',
                  padding: '0.75rem 1rem',
                  borderBottom: '1px solid gray',
                  fontWeight: 600,
                }}
              >
                {column.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, rowIndex) => (
            <tr key={rowIndex}>
              {columns.map((column) => {
                const value = row[column.key as keyof T];

                return (
                  <td
                    key={`${rowIndex}-${column.key}`}
                    style={{
                      padding: '0.75rem 1rem',
                      borderBottom: '1px solid gray',
                      verticalAlign: 'top',
                    }}
                  >
                    {String(value)}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Table;
